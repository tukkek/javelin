package javelin.model.world.location.town.diplomacy.quest.kill;

import javelin.controller.content.fight.mutator.mode.FightMode;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.labor.Trait;

/**
 * A {@link Haunt}-lite quest, that instead uses {@link Terrain}s for
 * {@link Map}s and {@link MonsterPool}s rather than {@link Branch}es as base
 * but with similar {@link FightMode}s.
 *
 * @see Trait#MILITARY
 * @author alex
 */
public class Raid extends KillQuest{

}
