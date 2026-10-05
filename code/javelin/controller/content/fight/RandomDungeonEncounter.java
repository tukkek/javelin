package javelin.controller.content.fight;

import javelin.controller.content.fight.mutator.Meld;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.Encounter;
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
  /** {@link Dungeon} constructor. */
  public RandomDungeonEncounter(DungeonFloor f){
    super(RPG.pick(f.encounters));
    set(Terrain.UNDERGROUND);
    mutators.add(new Meld());
    period=Period.NIGHT;
  }

  /** Picks a random map to use from this pool. */
  public void set(Terrain t){
    map=t.getmap();
    weather=t.getweather();
  }
}
