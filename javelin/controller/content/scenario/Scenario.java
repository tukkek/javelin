package javelin.controller.content.scenario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.action.world.meta.help.Guide;
import javelin.controller.content.event.urban.UrbanEvents;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.terrain.Water;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.content.wish.Win;
import javelin.controller.db.StateManager;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.Frequency;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.transport.Ship;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Caravan;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.LaborDeck;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.model.world.location.town.labor.expansive.Hub;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.old.RPG;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.SquadScreen;

/**
 * TODO scenario mode has been supplanted and disabled, might consider making
 * this an abstract class with {@link DungeonDelve} and {@link DungeonWorld}
 * depending on it instead to greatly simplify things. Scenario mode would be
 * nice as well but there's already 4 game modes, not to count {@link Minigame}s
 * and that's way too much feature creep to maintain.
 *
 * Scenario mode is a much faster type of gameplay than the main
 * {@link Campaign} mode. It's supposed to be finished on anywhere from 2 hours
 * of play to an afternoon (but of course it can be saved an resumed too).
 *
 * The world is a lot more static in this mode. Several features and
 * {@link Location}s are disabled - including {@link RandomEncounter}s in ohe
 * overworld map and {@link Hazard}s. The only "moving pieces" in the world map
 * are yourself and {@link Incursion}s.
 *
 * The {@link LocationGenerator} is disabled after the original world is
 * created, meaning that, wuthout random encounters and other infinite means of
 * gaining experience and loot, you are on a race against time to conquer all
 * hostile {@link Town}s - 1 to 3, with varying degress of power according to
 * the quantity in each game.
 *
 * There is only one enemy {@link Realm} per game and the starting features are
 * roughly made to be 1/3 neutral and 2/3 hostile.
 *
 * @see World#scenario
 */
public class Scenario implements Serializable{
  /** {@link World} size. */
  public int size=30;
  /**
   * Allow access to {@link Minigame}s or not.
   *
   * TODO may allow access but should fix them for scenario mode where their
   * {@link UniqueLocation}s aren't present.
   */
  public boolean minigames=false;
  /**
   * If <code>true</code>, {@link Town}s will be overidden after {@link World}
   * generation according to {@link #gettownel()}.
   */
  public boolean statictowns=true;
  /**
   * If not <code>null</code>, this amount will be seeded during {@link World}
   * generation. It will also be the cap as per {@link Frequency#max}.
   *
   * Number of starting dungeons in the {@link World} map. Since
   * {@link TempleKey}s are important to {@link Win}ning the game this should be
   * a fair amount, otherwise the player will depend only on {@link Caravan}s if
   * too many dungeons are destroyed or if unable to find the proper
   * {@link Chest} inside the dungeons he does find. Not that dungeons also
   * spawn during the course of a game but since this is highly randomized a
   * late-game player who ran out of dungeons should not be required to depend
   * on that alone.
   *
   * @see Actor#destroy(Incursion)
   * @see LocationGenerator
   */
  public Integer startingdungeons=null;
  /**
   * Whether {@link LocationGenerator} should continue placing features after
   * initial world generation.
   */
  public boolean respawnlocations=false;
  /**
   * If <code>true</code>, all hostile {@link Realm}s will be converted into a
   * single one. Mostly for use on scenarios without {@link Incursion}s anyway,
   * to make it easier to process visually.
   */
  public boolean normalizemap=true;
  /**
   * <code>true</code> if first {@link Town} should be located on
   * {@link Terrain#PLAIN} or {@link Terrain#HILL}.
   */
  public boolean easystartingtown=false;
  /** Minimum distance between {@link Desert} and {@link Water}. */
  public int desertradius=1;
  /** Number of {@link Town}s in the {@link World}. */
  public int towns=RPG.r(1,3)+1;
  /**
   * Will clear locations as indicated by {@link Fortification#clear}.
   */
  public boolean clearlocations=true;
  /** Wheter a full {@link LaborDeck} should be allowed. */
  public boolean allowlabor=false;
  /**
   * If <code>false</code>, only allow Actors marked as
   * {@link Actor#allowedinscenario}.
   */
  public boolean allowallactors=false;
  /** Ask for {@link Monster} names on {@link SquadScreen}. */
  public boolean asksquadnames=false;
  /** Wheter to cover {@link WorldTile}s. */
  public boolean fogofwar=false;
  /** If <code>false</code>, there will be no {@link RandomEncounter}s. */
  public boolean worldencounters=false;
  /** If <code>false</code>, there will be no {@link Hazard}s. */
  public boolean worldhazards=false;
  /** File name for the F1 help {@link Guide}. */
  public String helpfile="Scenario";
  /**
   * TODO highscores should be scenario-agnostic
   */
  public boolean record=false;
  /**
   * If <code>true</code>, will finish the game once there are no hostile
   * {@link Town}s.
   *
   * @see Town#ishostile()
   */
  public boolean dominationwin=true;
  /**
   * Number of {@link Location}s to spawn. Ideally we want the player finding a
   * new location every one or two steps into the unknown (fog of war), given
   * that the map scale is very concentrated.
   *
   * {@link LocationGenerator}.
   */
  public int startingfeatures=size*size/7;
  /** {@link Trove}s will only offer gold and experience rewards. */
  public boolean simpletroves=true;
  /**
   * Make random {@link RealmAcademy} and {@link Shop}s, insted of local
   * {@link Realm}.
   */
  public boolean randomrealms=true;
  /**
   * Affect labor and training speeds and amounts for XP and gold rewards.
   *
   * @see Labor#work(float)
   * @see RewardCalculator
   * @see Order
   */
  public int boost=3;

  /**
   * Generates {@link Actor}s and {@link Location}s during world generation and
   * during play.
   *
   * @see WorldGenerator
   * @see #respawnlocations
   */
  public Class<? extends LocationGenerator> locationgenerator=LocationGenerator.class;
  /** Multiplied to daily {@link Labor}. */
  public float labormodifier=1;
  /** Responsible for generation a {@link World} map. */
  public Class<? extends WorldGenerator> worldgenerator=WorldGenerator.class;
  /**
   * Whether to allow roads.
   *
   * @see World#roads
   * @see World#highways
   */
  public boolean roads=false;
  /** Whether to allow {@link Hub}s to spawn {@link Ship}s. */
  public boolean boats=false;
  /**
   * Adds this many squares to {@link District}s.
   *
   * @see District#getradius()
   */
  public int districtmodifier=0;
  /**
   * Whether all-flying or all-swimming {@link Squad}s can cross {@link Water}.
   * Does not impede {@link Transport}s from doing so.
   *
   * @see Squad#swim()
   * @see Monster#swim
   * @see Monster#fly
   */
  public boolean crossrivers=true;
  /** If <code>true</code> will generate {@link Quest}s. */
  public boolean quests=false;
  /** Whether to allow {@link UrbanEvents} in {@link Town}s. */
  public boolean urbanevents=false;
  /** How many days on average to call {@link Location#spawn()}. 0 = never. */
  public int spawnrate=30;

  /**
   * {@link Upgrade} or not the starting squad after it's been selected.
   *
   * @see SquadScreen
   */
  public void upgradesquad(Squad squadp){
    var squad=squadp.members;
    var members=new ArrayList<>(squad);
    var chosen=new HashSet<Kit>(members.size());
    while(!members.isEmpty()){
      var c=members.get(0);
      Kit kit=null;
      var kits=Kit.getpreferred(c.source,false);
      Collections.shuffle(kits);
      for(Kit k:kits){
        kit=k;
        if(!chosen.contains(kit)) break;
      }
      chosen.add(kit);
      c.source.customName=Character.toUpperCase(kit.name.charAt(0))
          +kit.name.substring(1);
      while(c.source.cr<6) kit.upgrade(c);
      members.remove(0);
    }
  }

  /**
   * @return <code>true</code> if the current selection is enough to start the
   *   game.
   * @see SquadScreen
   */
  public boolean checkfullsquad(ArrayList<Combatant> squad){
    return squad.size()>=4;
  }

  /**
   * @return <code>true</code> if the game has completed the mode's objective.
   */
  public boolean win(){
    return false;
  }

  /**
   * @return A prefix for the save game file.
   * @see StateManager
   */
  public String getsaveprefix(){
    return getClass().getSimpleName().toLowerCase();
  }

  /**
   * A special {@link Chest} is found in each {@link DungeonFloor}.
   *
   * @return Item inside the Chest.
   * @see #openaltar(Temple)
   */
  @SuppressWarnings("static-method")
  public Item openspecialchest(){
    throw new UnsupportedOperationException();
  }

  /**
   * Called at the end of each day. Might be called several times in a row - for
   * example: if a lone {@link Squad} decides to rest for a week, it'll be
   * called 7 times in a row.
   *
   * @param day
   */
  public void endday(){
    // nothing
  }

  /**
   * Setup or interact with the user before {@link World} generation starts.
   *
   * @see WorldGenerator
   */
  public void setup(){
    Squad.active=new SquadScreen().open();
  }

  /** Called when a Fight starts. */
  @SuppressWarnings("unused")
  public void start(Fight f,List<Combatant> blue,List<Combatant> red){
    // nothing by default
  }

  /** Called when a Fight ends. */
  @SuppressWarnings("unused")
  public void end(Fight f,boolean victory){
    // nothing by default
  }

  /**
   * Called only once, after {@link World} generation ends.
   *
   * @param w
   *
   * @see WorldGenerator#finish(Location, World)
   */
  @SuppressWarnings("unused")
  public void ready(){
    //nothing by default
  }

  /** Called at the end of @link World} or {@link DungeonFloor} actions. */
  public void endturn(){
    // nothing by default
  }

  @Override
  public String toString(){
    return helpfile;
  }

  /**
   * @param t Given to set things like {@link Town#population} and its garrison.
   * @param starting <code>true</code> if this is the player's starting town.
   * @see Town#populate(int)
   */
  public void populate(Town t,boolean starting){
    t.population=new int[]{20,15,10}[towns-2];
    t.populate(t.population);
  }
}
