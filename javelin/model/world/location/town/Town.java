package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.Tier;
import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.event.urban.UrbanEvents;
import javelin.controller.content.event.urban.negative.Riot;
import javelin.controller.content.event.urban.neutral.HostTournament;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.Siege;
import javelin.controller.content.fight.tournament.Exhibition;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.location.town.ShoreTownMap;
import javelin.controller.content.map.location.town.TownMap;
import javelin.controller.content.map.terrain.marsh.MarshShore.MarshTown;
import javelin.controller.content.scenario.Campaign;
import javelin.controller.content.scenario.Scenario;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.Realm;
import javelin.model.unit.Alignment;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.Caravan;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.LaborDeck;
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
 * TODO maybe override {@link #getel()} to return at least
 * {@link #population} so that weak {@link Incursion}s won't take big towns
 *
 * TODO keeping track of city names is pretty bad when it comes to diplomacy -
 * instead just have it be fire hamlet/water city and print their colors on the
 * world map
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
  public static final float DAILYLABOR=(float)Math.pow(20,2)/(400*15);

  /** @see HostTournament */
  public ArrayList<Exhibition> exhibitions=new ArrayList<>();
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

  /**
   * All natural resources linked to this town.
   *
   * TODO is not actually affecting economy right now
   */
  public Set<Resource> resources=new HashSet<>(0);
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
  /**
   * Records messages that should be shown to the player once he's in town.
   *
   * TODO add a message log in 2.0
   */
  public List<String> events=new ArrayList<>();
  /** {@link Mandate} options and diplomatic states. */
  public Diplomacy diplomacy=new Diplomacy(this);

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
    var list=new ArrayList<>(region);
    Collections.shuffle(list);
    var maxradius=Rank.CITY.getradius();
    var w=World.getseed();
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
  protected void generate(boolean water){
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
   * @return <code>true</code> if the town is hosting {@link #exhibitions}.
   */
  public boolean ishosting(){
    return !exhibitions.isEmpty();
  }

  @Override
  public void turn(long time,WorldScreen screen){
    if(ishosting()) exhibitions.remove(0);
    work();
    diplomacy.turn();
    Caravan.spawn(this);
  }

  void work(){
    if(strike>0){
      strike-=1;
      if(strike==0&&notifyplayer())
        events.add(this+" resumes its normal labors.");
      return;
    }
    var d=getdistrict();
    var labor=getdailylabor(true,d);
    if(labor>0) getgovernor().work(labor,d);
  }

  /**
   * @param randomize If <code>true</code>, result is modified by
   *   {@link RPG#randomize(int)}.
   * @param d
   * @return Daily amount of work done.
   * @see Labor#work(float)
   */
  public float getdailylabor(boolean randomize,District d){
    var resources=1+this.resources.size()*0.1f;
    var population=this.population;
    for(var l:d.getlocations()) population+=l.work;
    var labor=population*DAILYLABOR*resources*World.scenario.boost
        *World.scenario.labormodifier;
    if(randomize) labor+=RPG.randomize(Math.round(labor))/10f;
    return labor;
  }

  /**
   * @return Similar to {@link #getdailylabor(boolean)} but an integer for
   *   easier representation on UI.
   */
  public int getweeklylabor(boolean randomize){
    return Math.round(getdailylabor(randomize,getdistrict())*7);
  }

  /**
   * When a player captures a hostile town.
   *
   * @param showsurroundings if <code>true</code> will show this town's
   *   surrounding squares.
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
    var damage=RPG.randomize(4)+attacker.getel()/2;
    if(damage>0) population=Math.max(1,population-damage);
    setgovernor(new MonsterGovernor(this));
    diplomacy.clear();
  }

  /** Cancels any projects and replaces the {@link Governor}. */
  public void setgovernor(Governor g){
    if(governor!=null)
      for(Labor l:new ArrayList<>(governor.getprojects())) l.cancel();
    governor=g;
  }

  @Override
  public boolean interact(){
    if(!super.interact()) return false;
    Squad.active.lasttown=this;
    if(strike>0){
      var riots=this+" is rioting!";
      var d=Skill.DIPLOMACY.getbonus(Squad.active.getbest(Skill.DIPLOMACY));
      var k=Skill.KNOWLEDGE.getbonus(Squad.active.getbest(Skill.KNOWLEDGE));
      if(Math.max(d,k)>=strike){
        var eta=Math.max(1,strike+RPG.randomize(population));
        riots+="\nThe situation should resolve in around "+eta+" day(s).";
      }
      Javelin.message(riots,false);
    }else new TownScreen(this).show();
    for(final Combatant c:Squad.active.members)
      if(c.source.fasthealing>0) c.heal(c.maxhp,false);
    return true;
  }

  /**
   * TODO would be cool if the Town maps could be seeded per Town, per
   * {@link Tier}.
   *
   * @return {@link Fight} map.
   */
  public Map getmap(){
    var t=Tier.get(population);
    if(Terrain.MARSH.equals(Terrain.get(x,y))) return new MarshTown(t);
    if(Terrain.search(getlocation(),Terrain.WATER,1,World.seed)>0)
      return new ShoreTownMap(t);
    return new TownMap(t);
  }

  @Override
  protected Siege fight(){
    var s=new Siege(this);
    s.map=getmap();
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
    var image="town"+getrank().title.toLowerCase();
    if(!ishostile()&&ishosting()) image+="festival";
    return Images.get(List.of("world",image));
  }

  /** @see Rank */
  public Rank getrank(){
    for(var r:Rank.RANKS) if(population<=r.maxpopulation) return r;
    throw new RuntimeException("Unknown rank for population "+population);
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
    var actors=World.getall(Town.class);
    var towns=new ArrayList<Town>(actors.size());
    for(Actor a:actors) towns.add((Town)a);
    return towns;
  }

  /**
   * @return All Points in the game World that are districts.
   */
  public static HashSet<Point> getdistricts(){
    var districts=new HashSet<Point>();
    for(Town t:Town.gettowns()) districts.addAll(t.getdistrict().getarea());
    return districts;
  }

  @Override
  public void accessremotely(){
    if(ishostile()) super.accessremotely();
    else new GovernorScreen(this).show();
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
   *   the happenings of this town, <code>false</code> if it isn't conceivable
   *   that he would have access to these.
   * @see #ishostile()
   */
  public boolean notifyplayer(){
    return !ishostile();
  }

  /**
   * @param el Generates an initial {@link Location#garrison} from this
   *   suggested Encounter Level. This will be convered from a base EL to a
   *   party-equivalent EL.
   * @see Scenario#populate(Town, boolean)
   */
  public void populate(int el){
    el+=4;
    var t=Terrain.get(x,y);
    garrison.addAll(EncounterGenerator.generate(el,t));
  }

  @Override
  public Integer getel(){
    return population;
  }

  /** Displays relevant town news. */
  public void report(){
    if(!ishostile())
      for(var e:events) Javelin.message("News from "+this+"!\n"+e,true);
    events.clear();
  }

  /** Called when a {@link Squad} is present in Town. */
  public void enter(){
    for(var q:new ArrayList<>(diplomacy.quests)) q.claim();
    diplomacy.validate();
    report();
  }

  /** @return All discovered towns. */
  static public List<Town> getdiscovered(){
    var d=World.seed.discovered;
    return gettowns().stream().filter(t->d.contains(t.getlocation()))
        .collect(Collectors.toList());
  }
}
