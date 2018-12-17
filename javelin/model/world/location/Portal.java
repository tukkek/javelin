package javelin.model.world.location;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * Portals take a {@link Squad} from a place to another. They can also be used
 * to enter {@link PlanarFight}s.
 *
 * TODO a portal probably is more of an actor than a location...
 *
 * @author alex
 */
public class Portal extends Location{
	private static final String DESCRIPTION="A portal";

	private static final int NFEATURES=6;

	final Actor to;
	final Actor from;
	final boolean safe;
	final boolean instantaneous;
	/**
	 * Expires at this hour since the start of the game. If <code>null</code>
	 * never expire.
	 *
	 * @see Squad#hourselapsed
	 */
	public Long expiresat;
	final boolean wandering;
	/**
	 * An invasion portal is used by incoming {@link Incursion}s as an entry point
	 * to the {@link World}.
	 */
	public boolean invasion;

	/**
	 * @param fromp Source town.
	 * @param top Destination town.
	 * @param bidirectional If <code>true</code> will allow travel back.
	 * @param wanderingp If <code>true</code> the portal moves with time.
	 * @param safep If <code>false</code> deals damage upon entering.
	 * @param instantaneousp If <code>false</code> will take some time to get to
	 *          the other side.
	 * @param expiresatp See {@link #expiresat}.
	 * @param invasionp If <code>true</code> will periodically bring creatures
	 *          from the other side until closed.
	 */
	public Portal(Actor fromp,Actor top,boolean bidirectional,boolean wanderingp,
			boolean safep,boolean instantaneousp,Long expiresatp,boolean invasionp){
		super(DESCRIPTION);
		allowedinscenario=false;
		link=false;
		from=fromp;
		to=top;
		Point p=null;
		while(p==null)
			try{
				p=spawn(from);
			}catch(StackOverflowError e){
				continue;
			}
		x=p.x;
		y=p.y;
		if(bidirectional&&!invasionp)
			new Portal(to,from,false,wanderingp,safep,instantaneousp,expiresatp,false)
					.place();
		wandering=wanderingp;
		instantaneous=instantaneousp;
		expiresat=expiresatp;
		safe=safep;
		setinvasion(invasionp);
	}

	/**
	 * @param invasionp Changes all relevant field values according to this.
	 * @see #invasion
	 */
	public void setinvasion(boolean invasionp){
		invasion=invasionp;
		impermeable=!invasion;
		allowentry=invasion;
		if(invasion){
			realm=Realm.random();
			description="Invasion portal";
		}else{
			realm=null;
			description=DESCRIPTION;
		}
	}

	public Portal(Actor from,Actor to){
		this(from,to,Portal.activatefeature(),Portal.activatefeature(),
				!Portal.activatefeature(),!Portal.activatefeature(),
				Portal.activatefeature()?Squad.active.hourselapsed+RPG.r(7,30)*24:null,
				Portal.activatefeature());
	}

	static boolean activatefeature(){
		return RPG.r(1,NFEATURES+1)==1;
	}

	@Override
	protected void generate(){
		// do nothing
	}

	private Point spawn(Actor t){
		Point p=new Point(t.x,t.y);
		int size=World.scenario.size;
		ArrayList<Actor> actors=World.getactors();
		while(World.get(p.x,p.y,actors)!=null){
			p=new Point(determinedistance(t.x),determinedistance(t.y));
			if(p.x<0||p.x>=size||p.y<0||p.y>=size
					||World.getseed().map[p.x][p.y].equals(Terrain.WATER)){
				WorldGenerator.retry();
				p=new Point(t.x,t.y);
			}
		}
		return p;
	}

	private int determinedistance(int coordinate){
		int deltamin;
		int deltamax;
		if(RPG.r(1,2)==2){
			deltamin=+3;
			deltamax=+5;
		}else{
			deltamin=-5;
			deltamax=-3;
		}
		return RPG.r(coordinate+deltamin,coordinate+deltamax);
	}

	@Override
	public boolean interact(){
		if(invasion){
			MessagePanel.active.clear();
			Javelin.message("You close the invasion portal!",Javelin.Delay.NONE);
			InfoScreen.feedback();
			super.interact();
			return true;
		}
		// Key haskey = Key.use(Squad.active);
		// if (haskey != null) {
		// super.interact();
		// throw new StartBattle(new PlanarFight(haskey));
		// }
		travel();
		if(expiresat==null) super.interact();// remove
		return true;
	}

	void travel(){
		Point p=spawn(to);
		Squad.active.x=p.x;
		Squad.active.y=p.y;
		Squad.active.displace();
		Squad.active.place();
		String description="";
		if(!instantaneous){
			description+="It didn't seem that long on the way in...\n";
			Squad.active.hourselapsed+=RPG.r(1,7)*24;
		}
		if(!safe){
			description+="Ouch!";
			for(Combatant c:Squad.active.members)
				c.damage(Math.round(c.hp*RPG.r(1,3)/10f),c.source.energyresistance);
		}
		if(!description.isEmpty()){
			MessagePanel.active.clear();
			Javelin.message(description,Javelin.Delay.BLOCK);
			Javelin.input();
		}
	}

	public static Portal open(){
		ArrayList<Actor> towns=World.getall(Town.class);
		Actor from=RPG.pick(towns);
		Actor to=RPG.pick(towns);
		while(to==from)
			to=RPG.pick(towns);
		return new Portal(from,to);
	}

	@Override
	public void turn(long time,WorldScreen world){
		if(expiresat!=null&&Squad.active!=null
				&&Squad.active.hourselapsed>=expiresat){
			super.interact();
			return;
		}
		if(wandering){
			displace();
			place();
		}
	}

	@Override
	public Integer getel(Integer attackerel){
		if(!garrison.isEmpty()) return ChallengeCalculator.calculateel(garrison);
		assert !impermeable;
		return attackerel-4;
	}

	/**
	 * Opens a portal that a {@link TempleKey} can be safely used upon.
	 */
	public static void opensafe(){
		Portal p=Portal.open();
		p.setinvasion(false);
		p.expiresat=null;
		p.place();
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	protected boolean cancross(int tox,int toy){
		return true;
	}
}
