package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see Monster#burrow
 * @author alex
 */
public class Dig extends Action implements AiAction {
	/** Unique Dig instance to be created. */
	public static final Action SINGLETON = new Dig();

	private Dig() {
		super("Dig (requires burrow movement)", "d");
		allowburrowed = true;
	}

	@Override
	public boolean perform(Combatant hero) {
		if (hero.source.burrow == 0) {
			Javelin.message("Cannot burrow!", Javelin.Delay.WAIT);
			return false;
		}
		BattleState s = Fight.state;
		if (flooded(hero, s)) {
			return false;
		}
		Javelin.message(dig(hero, s), Javelin.Delay.WAIT);
		return true;
	}

	static boolean flooded(Combatant hero, BattleState s) {
		return s.map[hero.location[0]][hero.location[1]].flooded;
	}

	String dig(Combatant hero, BattleState s) {
		if (hero.burrowed) { // resurface
			hero.ap += Movement.toap(hero.source.burrow);
			hero.burrowed = false;
			hero.acmodifier -= 4;
			return hero + " unburrows...";
		} else if (s.isengaged(hero)) {// disengage + burrow
			hero.ap += Movement.disengage(hero);
			hero.burrowed = true;
			hero.acmodifier -= 4;
			return hero + " disengages...";
		} else { // burrow
			hero.ap += Movement.toap(hero.source.burrow);
			hero.burrowed = true;
			hero.acmodifier += 4;
			return hero + " burrows...";
		}
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant active, BattleState s) {
		ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>(
				1);
		if (active.source.burrow == 0 || flooded(active, s)) {
			return outcomes;
		}
		ArrayList<ChanceNode> outcome = new ArrayList<ChanceNode>(1);
		s = s.clone();
		active = s.clone(active);
		outcome.add(new ChanceNode(s, 1, dig(active, s), Javelin.Delay.BLOCK));
		return outcomes;
	}

	/**
	 * Show an error and refuse to do this while burrowed.
	 * 
	 * @throws RepeatTurn
	 */
	public static void refuse() {
		Javelin.message("Cannot do this while burrowed...", Javelin.Delay.WAIT);
		throw new RepeatTurn();
	}
}