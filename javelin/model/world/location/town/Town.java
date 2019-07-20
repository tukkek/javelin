package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.event.urban.UrbanEvents;
import javelin.controller.event.urban.negative.Riot;
import javelin.controller.event.urban.neutral.HostTournament;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Siege;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.location.TownMap;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.unit.Alignment;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.LaborDeck;
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
 * TODO maybe override {@link #getel(Integer)} to return at least
 * {@link #population} so that weak {@link Incursion}s won't take big towns
 *
 * TODO keeping track of city names is pretty bad when it comes to diplomacy -
 * instead just have it be fire hamlet/water city and print their colors on the
 * world map
 *
 * @author alex
 */
public class Town extends Location{
	/** @see #describehappiness() */
	public static final String NEUTRAL="Neutral";
	/** @see #describehappiness() */
	public static final String UNHAPPY="Unhappy";
	/** @see #describehappiness() */
	public static final String REVOLTING="Revolting";
	/** @see #describehappiness() */
	public static final String CONTENT="Content";
	/** @see #describehappiness() */
	public static final String HAPPY="Happy";
	static final int MINIMUMTOWNDISTANCE=Math
			.round(Math.round(District.RADIUSMAX*1.5));
	static final float HAPPINESSMAX=.1f;
	static final float HAPPINESSMIN=-HAPPINESSMAX;
	static final float HAPPINESSSTEP=.05f;
	static final float HAPPINESSDECAY=.001f;

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

	/** @see HostTournament */
	public ArrayList<Exhibition> events=new ArrayList<>();
	/**
	 * Represents the size of a town. In {@link Campaign} games this is expected
	 * to go from roughly 1 to 20 in the span of a year. Default is 1
	 *
	 * @see Scenario#populate(Town, boolean)
	 */
	public int population=1;
	/**
	 * Alphabetically ordered set of urban traits.
	 *
	 * @see LaborDeck
	 */
	public TreeSet<String> traits=new TreeSet<>();

	/** Remains the same even after capture. */
	public Realm originalrealm;

	/** Active quests. Updated daily. */
	public List<Quest> quests=new ArrayList<>(1);

	/**
	 * All natural resources linked to this town.
	 *
	 * TODO is not actually affecting economy right now
	 */
	public Set<Resource> resources=new HashSet<>(0);

	/**
	 * Percent value to apply to work done.
	 *
	 * @see Governor#work(float, District)
	 */
	float happiness=0;

	/**
	 * Each Town has an initial random alignment.
	 *
	 * @see Alignment#random()
	 */
	public Alignment alignment=Alignment.random();
	/**
	 * Days where the town will do no work. @
	 *
	 * @see Labor
	 * @see Riot
	 */
	public int strike=0;
	/** See {@link Governor}. */
	Governor governor=null;

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
	 * @return <code>true</code> if the town is hosting {@link #events}.
	 */
	public boolean ishosting(){
		return !events.isEmpty();
	}

	@Override
	public void turn(long time,WorldScreen screen){
		if(ishosting()) events.remove(0);
		work();
		if(happiness!=0) happiness+=happiness>0?-HAPPINESSDECAY:+HAPPINESSDECAY;
		updatequests();
	}

	void work(){
		if(strike>0){
			strike-=1;
			if(strike==0&&notifyplayer())
				Javelin.message(this+" resumes its normal labors.",true);
			return;
		}
		float labor=population+RPG.randomize(population)/10f;
		labor*=World.scenario.boost*World.scenario.labormodifier;
		if(labor<=0) return;
		labor*=DAILYLABOR*(1+gethappiness());
		getgovernor().work(labor,getdistrict());
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
		if(damage>0) population=Math.max(1,population-damage);
		setgovernor(new MonsterGovernor(this));
		for(var q:quests)
			q.cancel();
		quests.clear();
	}

	/** Cancels any projects and replaces the {@link Governor}. */
	public void setgovernor(Governor g){
		if(governor!=null) for(Labor l:new ArrayList<>(governor.getprojects()))
			l.cancel();
		governor=g;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(completequests()) return true;
		Squad.active.lasttown=this;
		if(strike>0){
			String riots=this+" is rioting!";
			var d=Skill.DIPLOMACY.getbonus(Squad.active.getbest(Skill.DIPLOMACY));
			var k=Skill.KNOWLEDGE.getbonus(Squad.active.getbest(Skill.KNOWLEDGE));
			if(Math.max(d,k)>=strike){
				var eta=Math.max(1,strike+RPG.randomize(population));
				riots+="\nThe situation should resolve in around "+eta+" day(s).";
			}
			Javelin.message(riots,false);
		}else
			new TownScreen(this).show();
		for(final Combatant c:Squad.active.members)
			if(c.source.fasthealing>0) c.heal(c.maxhp,false);
		return true;
	}

	boolean completequests(){
		var s=Squad.active;
		var completed=false;
		for(var q:new ArrayList<>(quests)){
			if(cancel(q)) continue;
			if(!q.complete()) continue;
			happiness+=HAPPINESSSTEP;
			completed=true;
			s.gold+=q.reward;
			String notification="You have completed a quest ("+q+")!\n";
			notification+="You are rewarded for your efforts with $"
					+Javelin.format(q.reward)+"!\n";
			notification+="Mood in "+this+" is now: "
					+describehappiness().toLowerCase()+".";
			Javelin.message(notification,true);
			quests.remove(q);
		}
		return completed;
	}

	boolean cancel(Quest q){
		if(!q.cancel()) return false;
		quests.remove(q);
		happiness-=HAPPINESSSTEP;
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
		return !ishostile()&&getgovernor().countprojects()>1;
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
		var name=getrank().title+" of "+description+'.';
		return ishostile()?describe(garrison,name,showgarrison,this):name;
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
			cancel(q);
		}
		var rank=getrank().rank;
		if(quests.size()<rank){
			var q=Quest.generate(this);
			if(q!=null) quests.add(q);
		}
	}

	/** @return Human representation of {@link #happiness}. */
	public String describehappiness(){
		if(happiness>=HAPPINESSMAX) return HAPPY;
		if(happiness>=HAPPINESSSTEP) return CONTENT;
		if(happiness<=-HAPPINESSMAX) return REVOLTING;
		if(happiness<=-HAPPINESSSTEP) return UNHAPPY;
		return NEUTRAL;
	}

	/**
	 * @return {@link #happiness} but bound to {@link #HAPPINESSMAX} either way.
	 */
	float gethappiness(){
		if(happiness>HAPPINESSMAX) return HAPPINESSMAX;
		if(happiness<-HAPPINESSMAX) return -HAPPINESSMAX;
		return happiness;
	}

	/** Should only be used for debug purposes. */
	@Deprecated
	public void sethappiness(float happiness){
		this.happiness=happiness;
	}

	/**
	 * Each town generates {@link Diplomacy#reputation} according to three
	 * factors: number of {@link #resources} and {@link #happiness} (both of which
	 * only count if the town is not hostile) and its {@link Relationship#status}.
	 *
	 * @return 0 or more reputation points based on {@link #happiness} and
	 *         {@link #resources}. Reputation never goes down so negative values
	 *         are returned as zero.
	 * @see #ishostile()
	 * @see Diplomacy#getdiscovered()
	 */
	public int generatereputation(){
		var r=Diplomacy.instance.getdiscovered().get(this);
		if(r==null) return 0;
		var reputation=r.getstatus();
		if(!ishostile())
			reputation+=resources.size()+Math.round(gethappiness()/HAPPINESSSTEP);
		return Math.max(0,reputation);
	}

	/**
	 * Generates and process {@link UrbanEvents}, if any. It should be unlikely
	 * but entirely possible that two towns have an event on the same day.
	 *
	 * @throws StartBattle For some events.
	 */
	public void dealevent(){
		if(!World.scenario.urbanevents||!RPG.chancein(UrbanEvent.CHANCE)) return;
		var squads=getdistrict().getsquads();
		var s=squads.isEmpty()?null:RPG.pick(squads);
		UrbanEvents.generating=this;
		UrbanEvents.instance.generate(s,population).happen(s);
	}

	/**
	 * @return <code>true</code> if the player should receive full information on
	 *         the happenings of this town, <code>false</code> if it isn't
	 *         conceivable that he would have access to these.
	 * @see #ishostile()
	 */
	public boolean notifyplayer(){
		return !ishostile();
	}

	/**
	 * @param el Generates an initial {@link Location#garrison} from this
	 *          suggested Encounter Level. This will be convered from a base EL to
	 *          a party-equivalent EL.
	 * @see Scenario#populate(Town, boolean)
	 */
	public void populate(int el){
		el+=4;
		var terrain=Terrain.get(x,y);
		while(garrison.isEmpty())
			try{
				garrison.addAll(EncounterGenerator.generate(el,terrain));
			}catch(GaveUp e){
				el+=1;
			}
	}

	@Override
	public Integer getel(Integer attackerel){
		return population;
	}
}
