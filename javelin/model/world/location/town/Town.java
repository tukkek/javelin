package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.fight.Siege;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.fight.tournament.Match;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.location.TownMap;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.basic.Growth;
import javelin.model.world.location.town.quest.Quest;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.NamingScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.GovernorScreen;
import javelin.view.screen.town.TownScreen;

/**
 * A hub for upgrading units, resting, participating in tournament, renting
 * transportation, buying items...
 *
 * Each town has it's own profile which is predetermined.
 *
 * @author alex
 */
public class Town extends Location{
	static final int MINIMUMTOWNDISTANCE=Math
			.round(Math.round(District.RADIUSMAX*1.5));

	/**
	 * How much {@link Labor} a single work produces in one day (
	 * {@value #DAILYLABOR}). The goal here is to have a {@link Town} controlled
	 * by a {@link Governor} be around {@link #population} 20 by the end of 1
	 * year.
	 *
	 * The best of verifying this with some degree of flexibility is to just
	 * verify that at the end of year 1, the town populations are between 16 and
	 * 25.
	 */
	public static final float DAILYLABOR=.11f;

	/** @see #ishosting() */
	public ArrayList<Exhibition> events=new ArrayList<>();
	/**
	 * Represent 10 working citizens that will produce 1 unit of {@link Labor}
	 * every 10 days.
	 *
	 * An arbitrary decision is to try to fit the game-span of a normal game into
	 * a 1-year period, which puts a town max size roughly at 10 if it does
	 * nothing but {@link Growth}.
	 */
	public int population=World.scenario.startingpopulation;
	/** See {@link Governor}. */
	private Governor governor=new HumanGovernor(this);
	/**
	 * Alphabetically ordered set of urban traits.
	 *
	 * @see Deck
	 */
	public TreeSet<String> traits=new TreeSet<>();

	/** Remains the same even after capture. */
	public Realm originalrealm;

	/** Active quests. Updated daily. */
	public List<Quest> quests=new ArrayList<>(1);

	/** @param p Spot to place town in the {@link World}. */
	public Town(Point p,Realm r){
		super(World.getseed().townnames.isEmpty()?null
				:World.getseed().townnames.remove(0));
		allowentry=false;
		if(p!=null){
			x=p.x;
			y=p.y;
		}
		realm=r;
		originalrealm=r;
		gossip=true;
		discard=false;
		vision=getdistrict().getradius();
	}

	/**
	 * @param list Selects a valid location from there.
	 * @throws RestartWorldGeneration If no valid location is found.
	 */
	public Town(HashSet<Point> list,Realm r){
		this(getvalidlocation(list),r);
	}

	static Point getvalidlocation(HashSet<Point> region){
		ArrayList<Point> list=new ArrayList<>(region);
		Collections.shuffle(list);
		int maxradius=Rank.CITY.getradius();
		World w=World.getseed();
		for(Point p:list)
			if(Terrain.get(p.x,p.y)!=Terrain.WATER&&checkboundary(p,maxradius,w.map)
					&&checksurroundings(p)&&Terrain.search(p,Terrain.WATER,1,w)<=4)
				return p;
		throw new RestartWorldGeneration();
	}

	static boolean checkboundary(Point p,int maxdistrictradius,Terrain[][] world){
		for(Point check:Point.getadjacentorthogonal()){
			check.x=p.x+check.x*maxdistrictradius;
			check.y=p.y+check.y*maxdistrictradius;
			if(!check.validate(0,0,world.length,world[0].length)) return false;
		}
		return true;
	}

	static boolean checksurroundings(Point p){
		for(Actor town:World.getall(Town.class))
			if(town.distance(p.x,p.y)<MINIMUMTOWNDISTANCE) return false;
		return true;
	}

	@Override
	protected void generate(){
		// location is given in the constructor
	}

	/**
	 * Receives a {@link #description} from the user for this town.
	 */
	public void rename(){
		description=NamingScreen.getname(toString());
	}

	@Override
	public District getdistrict(){
		return new District(this);
	}

	/**
	 * @return <code>true</code> if a flag icon is to be displayed.
	 * @see #events
	 */
	public boolean ishosting(){
		return !events.isEmpty();
	}

	/**
	 * Possibly starts a tournament in this town.
	 */
	public void host(){
		if(ishostile()) return;
		int nevents=RPG.r(3,7);
		for(int i=0;i<nevents;i++)
			events.add(RPG.r(1,2)==1?RPG.pick(Exhibition.SPECIALEVENTS):new Match());
	}

	@Override
	public void turn(long time,WorldScreen screen){
		if(ishosting())
			events.remove(0);
		else if(!ishostile()&&RPG.chancein(100)) host();
		float labor=population+RPG.randomize(population)/10f;
		labor*=World.scenario.boost*World.scenario.labormodifier;
		if(labor>0) getgovernor().work(labor*DAILYLABOR,getdistrict());
		updatequests();
	}

	/**
	 * When a player captures a hostile town.
	 *
	 * @param showsurroundings if <code>true</code> will show this town's
	 *          surrounding squares.
	 * @see #ishostile()
	 */
	public void captureforhuman(boolean showsurroundings){
		garrison.clear();
		setgovernor(new HumanGovernor(this));
		if(showsurroundings) Outpost.discover(x,y,Outpost.VISIONRANGE);
	}

	@Override
	public void captureforai(Incursion attacker){
		super.captureforai(attacker);
		garrison.clear();
		garrison.addAll(attacker.squad);
		attacker.remove();
		if(realm!=null) realm=attacker.realm;
		int damage=RPG.randomize(4)+attacker.getel()/2;
		if(damage>0) population-=Math.max(1,population-damage);
		setgovernor(new MonsterGovernor(this));
		quests.clear();
	}

	/** Cancels any projects and replaces the {@link Governor}. */
	public void setgovernor(Governor g){
		if(governor!=null) for(Labor l:new ArrayList<>(governor.getprojects()))
			l.cancel();
		governor=g;
	}

	@Override
	public Integer getel(Integer attackerel){
		return garrison.isEmpty()?Integer.MIN_VALUE
				:ChallengeCalculator.calculateel(garrison);
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		for(var q:new ArrayList<>(quests)){
			if(!q.complete()) continue;
			Squad.active.gold+=q.reward;
			String notification="You have completed a quest ("+q+")!\n";
			notification+="You are rewarded for your efforts with: $"
					+Javelin.format(q.reward)+"!";
			Javelin.message(notification,true);
			quests.remove(q);
		}
		Squad.active.lasttown=this;
		new TownScreen(this).show();
		for(final Combatant c:Squad.active.members)
			if(c.source.fasthealing>0) c.heal(c.maxhp,false);
		return true;
	}

	@Override
	protected Siege fight(){
		Siege s=new Siege(this);
		s.map=new TownMap(this);
		s.hide=false;
		s.bribe=true;
		return s;
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public boolean isworking(){
		return !ishostile()&&getgovernor().getprojectssize()>1;
	}

	@Override
	public Realm getrealmoverlay(){
		return ishostile()?super.getrealmoverlay():null;
	}

	@Override
	public Image getimage(){
		String image="locationtown"+getrank().title.toLowerCase();
		if(!ishostile()&&ishosting()) image+="festival";
		return Images.get(image);
	}

	/**
	 * @return A rank between [1,4] based on current {@link #population}.
	 * @see Rank#RANKS
	 */
	public Rank getrank(){
		for(int i=0;i<Rank.RANKS.length-1;i++){
			final Rank r=Rank.RANKS[i];
			if(population<=r.maxpopulation) return r;
		}
		return Rank.CITY;
	}

	@Override
	public void capture(){
		super.capture();
		captureforhuman(true);
	}

	/**
	 * Creates the initial {@link Location#garrison} for computer-controlled
	 * towns.
	 */
	public void populategarisson(){
		int el;
		if(World.scenario.statictowns){
			population=Scenario.getscenariochallenge();
			el=population;
		}else{
			setgovernor(new MonsterGovernor(this));
			el=RPG.r(1,5);
		}
		Terrain t=Terrain.get(x,y);
		while(garrison.isEmpty())
			try{
				garrison.addAll(EncounterGenerator.generate(el,t));
			}catch(GaveUp e){
				el+=1;
			}
	}

	/**
	 * @return All {@link World} towns.
	 */
	public static ArrayList<Town> gettowns(){
		ArrayList<Actor> actors=World.getall(Town.class);
		ArrayList<Town> towns=new ArrayList<>(actors.size());
		for(Actor a:actors)
			towns.add((Town)a);
		return towns;
	}

	/**
	 * @return All Points in the game World that are districts.
	 */
	public static HashSet<Point> getdistricts(){
		HashSet<Point> districts=new HashSet<>();
		for(Town t:Town.gettowns())
			districts.addAll(t.getdistrict().getarea());
		return districts;
	}

	@Override
	public void accessremotely(){
		if(ishostile())
			super.accessremotely();
		else
			new GovernorScreen(this).show();
	}

	@Override
	public String describe(){
		String difficulty="";
		if(ishostile()) difficulty=" ("+Difficulty.describe(garrison)+" fight)";
		return getrank().title+" of "+description+difficulty+'.';
	}

	/** @see #setgovernor(Governor) */
	public Governor getgovernor(){
		return governor;
	}

	/** Ticks a day off active quests and generates new ones. */
	public void updatequests(){
		if(!World.scenario.quests||ishostile()) return;
		for(var q:new ArrayList<>(quests)){
			q.daysleft-=1;
			if(q.daysleft==0||q.cancel()) quests.remove(q);
		}
		var rank=getrank().rank;
		while(quests.size()<rank){
			var quest=Quest.generate(this);
			if(quest==null) return;
			quests.add(quest);
		}
	}
}
