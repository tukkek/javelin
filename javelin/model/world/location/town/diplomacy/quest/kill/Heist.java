package javelin.model.world.location.town.diplomacy.quest.kill;

import javelin.controller.content.action.Withdraw;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.kit.Kit;
import javelin.controller.content.map.Map;
import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * A more involved and {@link Quest#LONG}-{@link Quest#term} type of kill quest.
 *
 * There are several marks on a manor-like {@link Map}, which can be scouted by
 * NPCs ahead of time - more competent ones are more expensive and will find
 * more information about more marks ahead of time. The levels of information
 * are: unknown, known (location show), type known, item known (and identified).
 * A scouting NPC is available per {@link Tier}, revealing 1d4 levels of 1d4
 * marks per tier (rolled using {@link RPG#advantage(int, int)}).
 *
 * During the {@link Fight}, constant reinforcements come in and players can
 * break {@link Tier}ed item cases to retrieve the {@link Item}s. If all
 * {@link Kit}-based guards are defeated, a chance to run away to safety with
 * any treasure is offered between waves but players can also {@link Withdraw}
 * normally at any time.
 *
 * @see Trait#CRIMINAL
 * @author alex
 */
public class Heist extends KillQuest{
	/**
	 * Average combat duration in rounds. This is a very tricky estimate, based on
	 * lack of sources, full reliance on anecdotal evidence and high-level combat
	 * being much more volatile (because of "whoever goes first wins" abilities).
	 *
	 * In my research, the one source I found for d20 proper suggested 4 rounds as
	 * median.
	 * https://paizo.com/threads/rzs2kgg1?On-average-how-many-rounds-does-combat-last
	 *
	 * A dozen or two discussions for other D&D editions and Pathfinder 2E boiled
	 * down to a median of 4.
	 *
	 * D&D 5e is designed around 3-round combats, with a couple "multiply damage
	 * by 3" guidelines that imply it in the DMG and MM.
	 */
	public static final int COMBATLENGTH=4;
}
