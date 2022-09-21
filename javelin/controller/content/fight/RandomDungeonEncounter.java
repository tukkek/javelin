package javelin.controller.content.fight;

import java.util.ArrayList;

import javelin.controller.content.fight.mutator.Meld;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.Period;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * Generates a {@link RandomEncounter} based on {@link Dungeon}
 * {@link Encounter}s. By default always uses a {@link Terrain#UNDERGROUND} map.
 *
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter{
  /** {@link Monster}s to be fought. */
  public Combatants encounter;

  /** {@link Dungeon} constructor. */
  public RandomDungeonEncounter(DungeonFloor f){
    set(Terrain.UNDERGROUND);
    mutators.add(new Meld());
    encounter=RPG.pick(f.encounters);
    period=Period.NIGHT;
  }

  /** Picks a random map to use from this pool. */
  public void set(Terrain t){
    map=t.getmap();
    weather=t.getweather();
  }

  @Override
  public Combatants generate(ArrayList<Combatant> blueteam){
    if(encounter==null||RandomEncounter.skip(encounter)) return null;
    return encounter.generate();
  }
}
