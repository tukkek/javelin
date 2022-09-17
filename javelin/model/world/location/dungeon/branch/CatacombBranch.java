package javelin.model.world.location.dungeon.branch;

import java.util.List;

import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.EncounterIndex;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.world.location.unique.Catacombs;

/**
 * TODO not a real branch yet, just tileset and {@link #MONSTERS}.
 *
 * @see Catacombs
 */
public class CatacombBranch extends Branch{
  /** Singleton. */
  public static final CatacombBranch INSTANCE=new CatacombBranch();
  /** Allowed {@link #MONSTERS}. */
  public static final List<MonsterType> TYPES=List.of(MonsterType.UNDEAD,
      MonsterType.VERMIN);
  /** Branch {@link Monster}s. */
  public static final EncounterIndex MONSTERS=new EncounterIndex(
      Monster.ALL.stream().filter(m->TYPES.contains(m.type)).toList());

  /** Constructor. */
  CatacombBranch(){
    super("Catacomb","of burials","floorcatacombs","wallcatacombs");
    terrains.add(Terrain.UNDERGROUND);
  }

  @Override
  public EncounterIndex getencounters(){
    return MONSTERS;
  }
}
