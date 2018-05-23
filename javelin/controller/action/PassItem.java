package javelin.controller.action;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Hands item over to a friendly unit.
 * 
 * @author alex
 */
public class PassItem extends Action {
	/** Unique instance for this class. */
	public static final Action SINGLETON = new PassItem();

	/** Constructor. */
	private PassItem() {
		super("Pass item to nearby ally", "p");
	}

	@Override
	public boolean perform(Combatant me) {
		final BattleState state = Fight.state;
		final Combatant sameme = state.clone(me);
		if (state.isengaged(sameme)) {
			Javelin.message("Disengage first...", Javelin.Delay.WAIT);
			return false;
		}
		final ArrayList<Combatant> surroudings = state.getsurroundings(sameme);
		for (final Combatant c : new ArrayList<Combatant>(surroudings)) {
			if (!c.isally(sameme, state) || state.isengaged(c)) {
				surroudings.remove(c);
			}
		}
		if (surroudings.isEmpty()) {
			Javelin.message("No unthreatened allies nearby...", Javelin.Delay.WAIT);
			return false;
		}
		final Item item = UseItem.queryforitemselection(me, false);
		if (item == null) {
			return false;
		}
		BattleScreen.active.center();
		int choice = Javelin.choose("Give this item to whom?", surroudings,
				surroudings.size() > 4, false);
		if (choice < 0) {
			throw new RepeatTurn();
		}
		final Combatant receiver = getmonster(surroudings.get(choice));
		final Combatant giver = getmonster(me);
		receiver.ap += ActionCost.PARTIAL;
		giver.ap += ActionCost.PARTIAL;
		Javelin.app.fight.getbag(giver).remove(item);
		Javelin.app.fight.getbag(receiver).add(item);
		return true;
	}

	static Combatant getmonster(final Combatant receiverc) {
		for (final Combatant c : Fight.state.getcombatants()) {
			if (c.toString() == receiverc.toString()) {
				return c;
			}
		}
		return null;
	}
}
