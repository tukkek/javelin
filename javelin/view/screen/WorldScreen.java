package javelin.view.screen;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.content.action.world.WorldAction;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.scenario.Scenario;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.StateManager;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.Period;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Features;
import javelin.model.world.location.dungeon.Portal;
import javelin.model.world.location.town.Town;
import javelin.old.Interface;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldPanel;

/**
 * Shows and helps manage the overworld view.
 *
 * @see World
 * @see JavelinApp#overviewmap
 * @author alex
 */
public class WorldScreen extends BattleScreen{
	/**
	 * Probabilisticaly, spawns a new {@link Actor} every this many days.
	 *
	 * @see LocationGenerator
	 */
	public static final int SPAWNPERIOD=14;
	/**
	 * Every {@link WorldMove} should be carefully considered - both to provide
	 * interesting strategic situation and also to help the {@link World} fit in a
	 * small amount of screen-space. If the World is small enough to fit a screen
	 * we can't have the player walking around too freely or he will be able to
	 * reach anywhere without difficulty.
	 *
	 * One encounter every 2 steps is way too restricting though - 3 feels like
	 * "most" steps will be safe, but just barely enough.
	 *
	 * Per D&D rules a well-equipped party should be able to withstand 4
	 * encounters in a row before needing to rest. This means that the player can
	 * take (statistically speaking) 6 steps away from a Town and still come back
	 * to rest properly. This sounds quite fine, even quite liberal, considering
	 * how small the map is and that the player can build roads, rent
	 * {@link Transport} vehicles, etc - which will further reduce the encounter
	 * rate by increasing player speed.
	 *
	 * The current value (1.85) has been selected to make sure starting units are
	 * close to the 3-step per encounter mark but actually a {@link Squad} with
	 * 30ft-moving units is getting closer to 5 steps even on bad {@link Terrain}.
	 * Unfortunately the fixed {@link WorldMove#MOVETARGET} and the fact that
	 * current {@link Monster} selection allows a novice player to select a
	 * 15-feet moving unit makes this hard to circumvent.
	 */
	public static final float HOURSPERENCOUNTER=WorldMove.MOVETARGET*1.85f;
	/** TODO used for tabulation, shouldn't be needed with a more modern UI */
	public static final String SPACER="                                               ";
	static final int STATUSSPACE=28;

	/** Last day that was taken into account by {@link World} computations. */
	public static double lastday=-1;
	/** Current active world screen. */
	public static WorldScreen current;
	static boolean welcome=true;
	public boolean firstdraw=true;

	/**
	 * Constructor.
	 *
	 * @param open
	 */
	public WorldScreen(boolean open){
		super(false,open);
	}

	@Override
	void open(){
		super.open();
		WorldScreen.current=this;
		Tile[][] tiles=gettiles();
		if(Debug.showmap)
			for(Tile[] ts:tiles)
				for(Tile t:ts)
					t.discovered=true;
		else
			showdiscovered(tiles);
	}

	@Override
	public void close(){
		super.close();
		savediscovered();
	}

	Tile[][] gettiles(){
		return mappanel.tiles;
	}

	void act(){
		try{
			if(callback!=null){
				callback.run();
				callback=null;
				Javelin.redraw();
				return;
			}
			var d=Squad.active.getdistrict();
			if(d!=null) d.town.enter();
			redraw();
			Interface.userinterface.waiting=true;
			final KeyEvent updatableUserAction=getUserInput();
			if(MapPanel.overlay!=null) MapPanel.overlay.clear();
			perform(updatableUserAction);
		}catch(RepeatTurn e){
			MessagePanel.active.clear();
			updateplayerinformation();
			act();
		}
	}

	void perform(KeyEvent keyEvent){
		if(keyEvent==null) return;
		for(final WorldAction a:WorldAction.ACTIONS)
			for(final String s:a.morekeys)
				if(s.equals(Character.toString(keyEvent.getKeyChar()))){
					MessagePanel.active.clear();
					a.perform(this);
					return;
				}
		for(final WorldAction a:WorldAction.ACTIONS)
			for(final int s:a.keys)
				if(s==keyEvent.getKeyCode()){
					MessagePanel.active.clear();
					a.perform(this);
					return;
				}
		throw new RepeatTurn();
	}

	@Override
	public void turn(){
		if(WorldScreen.welcome)
			saywelcome();
		else if(World.scenario.win()){
			StateManager.clear();
			System.exit(0);
		}else
			Javelin.lose();
		StateManager.save(false);
		endturn();
		if(World.getall(Squad.class).isEmpty()) return;
		if(Squad.active!=null) act();
		messagepanel.clear();
	}

	@Override
	public Point getsquadlocation(){
		return Squad.active==null?null:Squad.active.getlocation();
	}

	void redraw(){
		var h=JavelinApp.context.getsquadlocation();
		if(h!=null){
			center(h.x,h.y);
			view(h.x,h.y);
		}
		updateplayerinformation();
		Javelin.redraw();
	}

	@Override
	public void view(int x,int y){
		Squad.active.seesurroudings();
	}

	/** Marks coordinate as permanently visible. */
	static public void discover(int x,int y){
		if(!World.validatecoordinate(x,y)) return;
		WorldScreen s=getcurrentscreen();
		if(s!=null) s.gettiles()[x][y].discovered=true;
	}

	/**
	 * Player acts and ends turn, allowing time to pass.
	 *
	 * @see Javelin#act()
	 * @see Squad#time
	 */
	void endturn(){
		var s=World.scenario;
		s.endturn();
		if(Dungeon.active!=null) return;
		var act=Javelin.act();
		var time=Math.round(act==null?lastday*24:act.gettime());
		var day=Math.round(Math.round(Math.ceil(time/24.0)));
		while(day>WorldScreen.lastday||World.getall(Squad.class).isEmpty()){
			WorldScreen.lastday+=1;
			Season.change(day);
			Weather.weather();
			Portal.turn();
			World.seed.featuregenerator.spawn(1f/SPAWNPERIOD,false);
			s.endday();
			var actors=World.getactors();
			var incursions=Incursion.getall();
			actors.removeAll(incursions);
			for(var a:RPG.shuffle(actors)){
				a.turn(time,this);
				var l=a instanceof Location?(Location)a:null;
				if(s.spawnrate>0&&RPG.chancein(s.spawnrate)&&l!=null&&l.realm!=null
						&&l.ishostile()&&!l.garrison.isEmpty())
					l.spawn();
			}
			dofights(time,incursions);
			if(World.getall(Squad.class).isEmpty()) Javelin.redraw();
			if(Javelin.lose()) break;
			if(day-lastday>1){ //redraw day for long waits
				Javelin.redraw();
				updateplayerinformation();
			}
		}
	}

	/**
	 * Called from {@link #endturn()}, these daily activiities may throw a
	 * {@link StartBattle} and break normal code flow, so we leave them for last.
	 * If an exception is throw, nothing here should have been important to the
	 * {@link Scenario} in question.
	 */
	void dofights(long time,ArrayList<Incursion> incursions){
		for(var i:RPG.shuffle(incursions))
			i.turn(time,this);
		for(var t:RPG.shuffle(Town.gettowns()))
			t.dealevent();
	}

	/** Show party/world status. */
	public void updateplayerinformation(){
		Squad s=Squad.active;
		if(s==null) return;
		MessagePanel.active.clear();
		final ArrayList<String> infos=new ArrayList<>();
		String date="Day "+currentday();
		if(Dungeon.active==null){
			infos.add(date+", "+Period.now().toString().toLowerCase());
			String season=Season.current.toString();
			String weather=Terrain.current().describeweather();
			if(!weather.isEmpty()) season+=", "+weather+"";
			infos.add(season);
		}else
			infos.add(Dungeon.active.toString());
		infos.add("");
		if(Dungeon.active==null){
			final int mph=s.speed(Terrain.current(),Squad.active.x,Squad.active.y);
			infos.add(mph+" mph"+(s.transport==null?"":s.transport.load(s.members)));
		}
		infos.add(printgold());
		final ArrayList<String> hps=showstatusinformation();
		while(hps.size()>6)
			hps.remove(6);
		String panel="";
		for(int i=0;i<Math.max(infos.size(),hps.size());i++){
			String hp;
			final String info=infos.size()>i?"    "+infos.get(i):"";
			if(hps.size()>i){
				hp=hps.get(i);
				while(hp.length()<WorldScreen.SPACER.length())
					hp+=" ";
			}else
				hp=WorldScreen.SPACER;
			panel+=hp+info+"\n";
		}
		Javelin.message(panel,Javelin.Delay.NONE);
	}

	static String printgold(){
		final int upkeep=Squad.active.getupkeep();
		String gold="$"+Javelin.format(Squad.active.gold);
		if(upkeep>0) gold+=" (upkeep: $"+Javelin.format(upkeep)+"/day)";
		return gold;
	}

	/**
	 * @return One line of text containing unit name and status information
	 *         (health, poison, etc).
	 */
	static public ArrayList<String> showstatusinformation(){
		final ArrayList<String> hps=new ArrayList<>();
		for(final Combatant c:Squad.active.members){
			String status=c.getstatus()+", ";
			if(c.source.poison>0) status+="weak, ";
			if(c.spells.size()>0&&checkexhaustion(c)) status+="spent, ";
			String vital=c.toString()+" ("+status.substring(0,status.length()-2)+")";
			while(vital.length()<WorldScreen.STATUSSPACE)
				vital+=" ";
			long cr=Math.round(Math.floor(c.source.cr));
			hps.add(vital+" Level "+cr+" "+c.gethumanxp());
		}
		return hps;
	}

	static private boolean checkexhaustion(Combatant m){
		for(Spell s:m.spells)
			if(!s.exhausted()) return false;
		return true;
	}

	/**
	 * @return The current day, starting from 1 when the game begins.
	 */
	static public long currentday(){
		return Math.round(Math.floor(WorldScreen.lastday));
	}

	private void saywelcome(){
		if(Dungeon.active==null) Squad.updatevision();
		Javelin.redraw();
		Javelin.message(Javelin.welcome(),Javelin.Delay.NONE);
		WorldScreen.current.center();
		InfoScreen.feedback();
		messagepanel.clear();
		WorldScreen.welcome=false;
	}

	/**
	 * The arbitrary rule is 1 encounter per day in the wild.
	 *
	 * TODO think of a better framework as design guideline for distance between
	 * towns, encounter ratio, etc. The game has changed a lot since 1.0 but the
	 * goal back then was to allow you to reach a new {@link Town} after an
	 * average of 4 fights (100% resource spending). Now {@link Fortification}s
	 * and hostile Towns change everything. The ultimate goal still is to make
	 * every move strategically meaningful (no no-brainers) while still keeping
	 * the world scre en smal enough to fit in just a few screens worth of size.
	 *
	 * @return <code>true</code> if exploration was uneventful, <code>false</code>
	 *         if something happened.
	 */
	public boolean explore(int x,int y){
		Squad s=Squad.active;
		float hoursellapsed=s.move(false,Terrain.current(),x,y);
		if(World.scenario.worldencounters&&(s.transport==null||s.transport.battle())
				&&!Town.getdistricts().contains(new Point(x,y)))
			RandomEncounter.encounter(hoursellapsed/HOURSPERENCOUNTER);
		if(!World.scenario.worldhazards) return true;
		boolean special=RPG.r(1,Terrain.HAZARDCHANCE)==1;
		if(s.getdistrict()!=null) return true;
		var hazards=Terrain.get(x,y).gethazards(special).stream()
				.filter(h->h.validate()).collect(Collectors.toList());
		if(hazards.isEmpty()) return true;
		RPG.pick(hazards).hazard(Math.round(hoursellapsed));
		return false;
	}

	/**
	 * Gives a chance for this context to react to a {@link WorldMove} (such as
	 * delegating to {@link Actor}s or {@link Features}.
	 *
	 * @param x Target coordinate.
	 * @param y Target coordinate.
	 * @return If <code>true</code>, means the context has handled the effects of
	 *         this particular movement (including player placement). It may also
	 *         prevent further moves in a sequence by flagging
	 *         {@link WorldMove#abort}.
	 */
	public boolean react(int x,int y){
		if(!World.seed.map[x][y].enter(x,y)){
			WorldMove.abort=true;
			return false;
		}
		Squad s=Squad.active;
		s.lastterrain=Terrain.current();
		Actor actor=World.get(x,y,World.getactors());
		if(actor==null) return false;
		Location l=actor instanceof Location?(Location)actor:null;
		try{
			if(actor.interact()) return true;
			if(l!=null&&l.allowentry&&!l.ishostile()) WorldMove.place(x,y);
			return true;
		}catch(StartBattle e){
			if(l!=null&&l.allowentry) WorldMove.place(x,y);
			throw e;
		}
	}

	/**
	 * @return <code>false</code> if the given coordinate is impenetrable or
	 *         impassable.
	 */
	public boolean allowmove(int x,int y){
		return true;
	}

	/** Updates the hero to this new location. */
	public void updatelocation(int x,int y){
		Squad.active.x=x;
		Squad.active.y=y;
		Squad.active.updateavatar();
	}

	@Override
	public void center(int x,int y){
		if(firstdraw){
			mappanel.setposition(x,y);
			firstdraw=false;
		}else
			mappanel.viewposition(x,y);
	}

	@Override
	public Image gettile(int x,int y){
		return Images.get(List.of("terrain",Terrain.get(x,y).toString()));
	}

	/**
	 * @return <code>true</code> if this {@link World} coordinate has been and
	 *         still is discovered.
	 */
	public static boolean see(Point p){
		return World.validatecoordinate(p.x,p.y)
				&&getcurrentscreen().gettiles()[p.x][p.y].discovered;
	}

	static WorldScreen getcurrentscreen(){
		if(BattleScreen.active instanceof WorldScreen)
			return (WorldScreen)BattleScreen.active;
		return null;
	}

	/**
	 * @return A random encounter fight.
	 */
	public Fight encounter(){
		return new RandomEncounter();
	}

	@Override
	protected MapPanel getmappanel(){
		return new WorldPanel();
	}

	/**
	 * @return <code>true</code> if this coordinate is valid in this context.
	 */
	public boolean validatepoint(int x,int y){
		return World.validatecoordinate(x,y);
	}

	public void adddiscovered(HashSet<Point> discovered){
		discovered.clear();
		for(Tile[] ts:current.mappanel.tiles)
			for(Tile t:ts)
				if(t.discovered) discovered.add(new Point(t.x,t.y));
	}

	void showdiscovered(Tile[][] tiles){
		for(Point p:getdiscovered())
			tiles[p.x][p.y].discovered=true;
	}

	/**
	 * This is intended for use by {@link StateManager} and does not maintain a
	 * live set. Prefer using {@link Tile#discovered} directly.
	 *
	 * @return Discoveed tiles.
	 */
	protected HashSet<Point> getdiscovered(){
		return World.seed.discovered;
	}

	public void savediscovered(){
		adddiscovered(getdiscovered());
	}

	@Override
	public void center(){
		Javelin.app.switchScreen(this);
		Point here=getsquadlocation();
		if(here!=null) center(here.x,here.y);
	}
}
