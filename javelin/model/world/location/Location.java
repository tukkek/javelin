package javelin.model.world.location;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.LocationFight;
import javelin.controller.fight.Siege;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Diplomacy;
import javelin.model.unit.skill.Knowledge;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.Images;
import javelin.view.mappanel.world.WorldMouse;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * A {@link Actor} that is actually a place that represent a location to be
 * entered or explored.
 *
 * Im may be tempted to add a uniform #level here for subclass convenience, but
 * having the same on each subclass that needs it bring more benefits, from code
 * efficiency to ease of searching calls and assignments. The very small
 * convenience added is not worth the trade-off.
 *
 * @see World#getactors()
 * @author alex
 */
public abstract class Location extends Actor{
	/**
	 * Note that this value is used in a triagonal calculation, not in
	 * square-steps.
	 */
	static final int CLOSE=4;

	/**
	 * If <code>false</code> will make sure no {@link Squad} occupies the same
	 * {@link World} space as this.
	 */
	public boolean allowentry=true;

	/**
	 * Represent a list of units positioned inside a world feature.
	 */
	public Combatants garrison=new Combatants();
	/**
	 * If <code>true</code>, the computer will destroy this instead of positioning
	 * a {@link #garrison} here.
	 *
	 * @see #destroy(javelin.model.world.Incursion)
	 */
	public boolean sacrificeable=false;
	/**
	 * If this feature should be {@link #remove()}d after {@link #interact()}.
	 */
	public boolean discard=true;
	/**
	 * Used for {@link #toString()}.
	 */
	public String description;
	/**
	 * Allows use of {@link Diplomacy}.
	 */
	public boolean gossip=false;

	/**
	 * Used to calculate fog of war. Usually rangess from 1 (most
	 * {@link Fortification}s, to 3 ({@link Outpost}s), with {@link Town}s being
	 * the benchmark for 2. {@link UniqueLocation}s usually have none since
	 * they're minding their own business.
	 */
	protected int vision=0;

	/** Link with a road if nearby. */
	public boolean link=true;

	/**
	 * If <code>false</code>, will not show individual units.
	 *
	 * @see WorldMouse
	 * @see Squad#spot(List)
	 */
	protected boolean showgarrison=true;

	/**
	 * @param descriptionknown What to show a player on a succesfull
	 *          {@link Knowledge} check.
	 * @param descriptionunknown What to show otherwise.
	 */
	public Location(String description){
		super();
		this.description=description;
	}

	/**
	 * Determines {@link World} location for this.
	 */
	protected void generate(){
		generate(this,false);
	}

	/**
	 * Default implementation of {@link #generate()}, will try random positioning
	 * a {@link Actor} until it lands on a free space.
	 *
	 * @param allowwater <code>true</code> if it is allowed to place the actor on
	 *          {@link Terrain#WATER}.
	 */
	static public void generate(Actor p,boolean allowwater){
		p.x=-1;
		ArrayList<Actor> actors=World.getactors();
		actors.remove(p);
		final World w=World.getseed();
		int size=World.scenario.size-1;
		while(p.x==-1||!allowwater&&w.map[p.x][p.y].equals(Terrain.WATER)
				||World.get(p.x,p.y,actors)!=null||neartown(p)||w.roads[p.x][p.y]
				||w.highways[p.x][p.y]){
			p.x=RPG.r(0,size);
			p.y=RPG.r(0,size);
			WorldGenerator.retry();
		}
	}

	/**
	 * @return <code>true</code> if given actor is too close to a town.
	 */
	static boolean neartown(Actor p){
		return p.getdistrict()!=null;
	}

	@Override
	public boolean interact(){
		return interact(this);
	}

	/**
	 * Called when this place is reached with the active Squad.
	 *
	 * @throws StartBattle
	 * @return <code>true</code> if the {@link Squad} is able to interact with
	 *         this place, <code>false</code> if a {@link #garrison} defends it.
	 */
	public static boolean interact(Location l){
		if(l.defend()) return false;
		if(l.gossip) gossip(l);
		if(l.discard) l.remove();
		return true;
	}

	static void gossip(Location here){
		Actor closest=null;
		ArrayList<Actor> actors=World.getactors();
		for(int x=here.x-5;x<=here.x+5;x++)
			for(int y=here.y-5;y<=here.y+5;y++){
				if(WorldScreen.see(new Point(x,y))) continue;
				Actor a=World.get(x,y,actors);
				if(a==null) continue;
				if(closest==null
						||a.distance(here.x,here.y)<closest.distance(here.x,here.y))
					closest=a;
			}
		if(closest==null) return;
		int diplomacy=Squad.active.getbest(Skill.DIPLOMACY)
				.taketen(Skill.DIPLOMACY);
		if(diplomacy>=10+closest.distance(here.x,here.y))
			WorldScreen.discover(closest.x,closest.y);
	}

	@Override
	public Boolean destroy(Incursion attacker){
		if(impermeable||attacker.realm==realm) return Incursion.ignore(attacker);
		if(sacrificeable){
			int el=attacker.getel();
			return Incursion.fight(el,getel(el));
		}
		captureforai(attacker);
		return false;
	}

	/** TODO could probably merge this and {@link #capture()}. */
	protected void captureforai(Incursion attacker){
		garrison.addAll(attacker.squad);
		realm=attacker.realm;
	}

	/**
	 * @return <code>true</code> if this place has a hostle {@link #garrison}
	 *         presence.
	 */
	public boolean ishostile(){
		return !garrison.isEmpty();
	}

	/**
	 * @throws StartBattle If the player chooses to attack a defended location.
	 * @return <code>false</code> if this is unguarded, <code>true</code> if it is
	 *         guarded but the player aborted combat.
	 */
	protected boolean defend(){
		if(!ishostile()) return false;
		if(Debug.disablecombat){
			capture();
			return false;
		}
		if(headsup(describe())) throw new StartBattle(fight());
		throw new RuntimeException("headsup sould throw #wplace");
	}

	/**
	 * @return Battle controller.
	 * @throws RepeatTurn
	 */
	protected Fight fight(){
		return new Siege(this);
	}

	/**
	 * Offers information and a chance to back out of the fight.
	 *
	 * @param opponents Will be {@link Squad#spot}ted.
	 * @param description See {@link #describe()}.
	 * @return <code>true</code> if player confirms engaging in battle.
	 * @throws RepeatTurn
	 */
	static public boolean headsup(String description){
		MessagePanel.active.clear();
		final String prompt=description+"\n\n"
				+"Press s to storm or any other key to retreat.";
		Javelin.message(prompt,Javelin.Delay.NONE);
		if(InfoScreen.feedback()=='s') return true;
		throw new RepeatTurn();
	}

	/** @return A {@link LocationFight}-like description. */
	public static String describe(List<Combatant> opponents,String name,
			boolean showgarrison,Actor a){
		var description=name;
		if(!opponents.isEmpty()){
			description+=" ("+Difficulty.describe(opponents)+" fight).";
			if(showgarrison) description+="\n\nSpotted: "
					+Squad.active.spotenemies(opponents,a)+"...";
		}
		return description;
	}

	@Override
	public String describe(){
		return describe(garrison,toString(),showgarrison,this);
	}

	@Override
	public String toString(){
		return description;
	}

	@Override
	public Image getimage(){
		return Images.get(getimagename());
	}

	/**
	 * @return The filename for {@link #getimage()}, without extension.
	 */
	public String getimagename(){
		return "location"+getClass().getSimpleName().toLowerCase();
	}

	/**
	 * Convenience method for subclasses to use as {@link #generate()}. Warning:
	 * when using this do not override {@link #generate(Location)}!
	 */
	protected void generateawayfromtown(){
		x=-1;
		while(x==-1||neartown(this))
			generate(this,false);
	}

	/**
	 * @param targets Class of other places to verify if nearby.
	 * @return <code>true</code> if there is one of the given places under
	 *         {@value #CLOSE} distance from here.
	 * @see Walker#distance(int, int, int, int)
	 */
	public boolean isnear(Class<? extends Location> targets,int distance){
		Actor nearest=findnearest(targets);
		return nearest!=null&&distance(nearest.x,nearest.y)>distance;
	}

	public boolean isnear(Class<? extends Location> targets){
		return isnear(targets,CLOSE);
	}

	/**
	 * @return Total number of Locations in the game {@link World}.
	 */
	public static int count(){
		int sum=0;
		for(ArrayList<Actor> places:World.getseed().actors.values())
			sum+=places.size();
		return sum;
	}

	/**
	 * @return <code>true</code> if a item icon is to be displayed.
	 */
	public boolean hascrafted(){
		return false;
	}

	/**
	 * @return <code>true</code> if a arrow icon is to be displayed.
	 */
	public boolean hasupgraded(){
		return false;
	}

	/**
	 * @return <code>true</code> if should render the {@link Images#HOSTILE}
	 *         overlay.
	 */
	public boolean drawgarisson(){
		return ishostile();
	}

	@Override
	public void place(){
		if(x==-1) generate();
		super.place();
	}

	/** @return 0 if {@link #ishostile()} or {@link #vision}. */
	public int watch(){
		return ishostile()?0:vision;
	}

	/**
	 * Clear {@link #garrison} and {@link Realm}.
	 */
	public void capture(){
		garrison.clear();
		realm=null;
	}

	/**
	 * Should only be called from a valid {@link District}.
	 *
	 * @param d
	 *
	 * @return Any upgrades this location may be improved with.
	 */
	public ArrayList<Labor> getupgrades(District d){
		return new ArrayList<>(0);
	}

	public void rename(String name){
		description=name;
	}

	public boolean isworking(){
		return false;
	}

	public boolean canupgrade(){
		return !ishostile();
	}

	public void spawn(){
		Incursion.spawn(new Incursion(x,y,garrison,realm));
		garrison=EncounterGenerator.generate(getel(null),Terrain.get(x,y));
	}

	@Override
	public Integer getel(Integer attackerel){
		return garrison.isEmpty()?Integer.MIN_VALUE
				:ChallengeCalculator.calculateel(garrison);
	}

	/**
	 * @param p Places the location at this point of the map. Note that forcing
	 *          placement skips {@link #generate()}.
	 * @see #place()
	 */
	public void place(Point p){
		setlocation(p);
		place();
	}
}