package javelin.controller.action;

import javelin.model.unit.CurrentAttack;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;

/**
 * Utility class for more idyommatic access to different types of
 * {@link Combatant#ap} costs.
 * 
 * TODO this was introduced during the development of 1.7 and as such most of
 * the code doesn' use it even though it uses the same assumptions underlined
 * here. It would be nice to look at all the methods using {@link Combatant#ap}
 * and convert them into this format though.
 * 
 * @author alex
 */
public class ActionCost {
	/**
	 * A full-round action, equal to a full action point (1ap). Example: using
	 * an entire {@link AttackSequence} (even though this is done non-linearly
	 * in the game - but eventually reaching 1ap after all attacks are made).
	 * 
	 * @see CurrentAttack
	 */
	public static final float FULL = 1f;
	/**
	 * You can make both a {@link #STANDARD} and a {@link #MOVE} action per
	 * turn. It makes sense then that each would take half the action points of
	 * a {@link #FULL} action.
	 */
	public static final float PARTIAL = FULL / 2f;
	/**
	 * Standard action. Example: making a single {@link Attack} from an
	 * {@link AttackSequence} (usually using the first one, which is the best,
	 * by convention) - note though that this is only done on special cases such
	 * as {@link Charge} or {@link Strike}s - normal attacks are handled by
	 * {@link CurrentAttack} and as such as considered a {@link #FULL} action
	 * that is broken into smaller actions for each attack in the sequence.
	 */
	public static final float STANDARD = PARTIAL;
	/**
	 * Move-equivalent action. Example: walking as many feet as an unit's speed
	 * allows.
	 */
	public static final float MOVE = PARTIAL;
	/**
	 * Described as very fast, as it can be done only once per turn without
	 * affecting anything else. Making it half of a {@link #STANDARD} action
	 * seems to satifsty this, as more than one use per turn would result in the
	 * same amount as an actual {@link #PARTIAL} action.
	 */
	public static final float SWIFT = PARTIAL / 2f;
	/**
	 * Just like a {@link #SWIFT} action, this is considered something you can
	 * only do once per turn. Hence, it has the same value.
	 * 
	 * Note that this has an edge case of making using both a
	 * {@link #FIVEFOOTSTEP} and a {@link #SWIFT} action would result in the
	 * same AP cost of a {@link #STANDARD} action, while it should, ideally, be
	 * treated as a free-action (0ap). This would be the same as using 2
	 * {@link #SWIFT} action in a single turn. That's not very worrying or game
	 * breaking though (especially considering {@link #SWIFT} actions are pretty
	 * rare - but it could be improved upon with some math. TODO
	 */
	public static final float FIVEFOOTSTEP = SWIFT;
}