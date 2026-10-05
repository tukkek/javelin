package javelin.controller.content.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.Debug;
import javelin.JavelinApp;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.Tier;
import javelin.controller.comparator.EncountersByEl;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.generator.encounter.EncounterMixer;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * {@link Fight} that happens on the {@link WorldScreen}. These are limited to
 * {@link Tier#MID} for {@link Terrain#safe} and {@link Tier#HIGH} elsewhere but
 * with a generous double-{@link RPG#low(int, int)} for EL selection and also
 * {@link Encounter} selection (otherwise the game would be too hostile for
 * low-level {@link Squad}s and new players).
 *
 * This also allows consistent {@link Tier#HIGH} challenges to be relegated to
 * special {@link Location}s (such as {@link Wilderness}es) (and
 * {@link Tier#EPIC} to end-game content) while still having a window of
 * opportunity for higher challenges to show up as random encounters so the
 * {@link World} continues to be dangerous on the long-term.
 *
 * {@link World#encounters} are static but {@link #evolve()} over time. They are
 * kept ordered from low to high EL and encounters are both generated and
 * selected using {@link RPG#low(int, int)}. This generates gentler
 * {@link Fight}s for new {@link Squad}s and players, while creating a
 * naturalistic {@link World} where low prey is more common than apex predators
 * - while still allowing even {@link Difficulty#DEADLY} fights for low-tier and
 * mid-tier {@link Squad}s on 1-10% of {@link Encounter}s, on
 * {@link Terrain#safe} and unsafe, respectively.
 *
 * This enables both that the {@link World} be largely safe to explore even
 * early on, while also being very dangerous for most of the leveling process on
 * a minority of cases - allowing players to feel very fast that they are
 * outgrowing typical encounters while only allowing for them to feel truly safe
 * when they are closing to reach {@link Tier#EPIC}.
 *
 * @see <a href="https://github.com/tukkek/javelin/issues/320">Random World
 *   Encounter redesign</a>
 * @author alex
 */
public class RandomEncounter extends Fight{
  static final int ENCOUNTERS=20;
  static final int REFRESHDIE=4;
  static final int REFRESHCHANCE=Math
      .round((1+REFRESHDIE)/2*2*Season.SEASONDURATION/ENCOUNTERS);

  /** Foes. */
  public Combatants enemies;

  /** Internal constructor. */
  protected RandomEncounter(Combatants c){
    enemies=new EncounterMixer(c).mix();
  }

  /** Constructor. */
  public RandomEncounter(){
    this(generate());
  }

  static Combatants generate(){
    var encounters=World.seed.encounters.get(Terrain.current());
    var e=encounters.get(RPG.low(0,encounters.size()-1));
    return new Combatants(e);
  }

  @Override
  public Combatants generate(ArrayList<Combatant> blueteam){
    return enemies.generate();
  }

  @Override
  public boolean avoid(List<Combatant> foes){
    return foes==null||super.avoid(foes);
  }

  @Override
  public Integer getel(int teamel){
    return enemies.getel();
  }

  /** @param chance % chance of starting a battle. */
  static public void encounter(double chance){
    if(Debug.disablecombat||RPG.random()>=chance) return;
    var f=JavelinApp.context.encounter();
    if(f==null) return;
    var el=Squad.active.getel();
    if(Difficulty.calculate(el,f.getel(el))>Difficulty.VERYEASY)
      throw new StartBattle(f);
  }

  static void generate(List<Combatants> encounters,int target,Terrain t){
    while(encounters.size()<target){
      var tier=t.safe?Tier.LOW:Tier.MID;
      var e=EncounterGenerator.generate(RPG.low(1,tier.maxlevel),t);
      if(e!=null) encounters.add(e);
    }
    encounters.sort(EncountersByEl.INSTANCE);
  }

  /** @see World#encounters */
  public static void generate(World w){
    for(var t:Terrain.NONUNDERGROUND){
      var target=RPG.randomize(ENCOUNTERS,1,Integer.MAX_VALUE);
      var encounters=new ArrayList<Combatants>(target);
      generate(encounters,target,t);
      w.encounters.put(t,encounters);
    }
  }

  /**
   * Modify {@link World#encounters} over time. The goal is to have half the
   * {@link Encounter}s changed with every {@link Season#SEASONDURATION}. Called
   * daily.
   *
   * @see WorldScreen
   */
  public static void evolve(){
    for(var t:Terrain.NONUNDERGROUND){
      if(!RPG.chancein(REFRESHCHANCE)) continue;
      var encounters=World.seed.encounters.get(t);
      var trim=encounters.size()-RPG.r(1,REFRESHDIE);
      if(trim<1) trim=1;
      while(encounters.size()>trim)
        encounters.remove(RPG.r(0,encounters.size()-1));
      generate(encounters,encounters.size()+RPG.r(1,REFRESHDIE),t);
    }
  }
}
