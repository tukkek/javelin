package javelin.controller.action;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.InfoScreen;

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
			Game.message("You are engaged in combat!", Delay.NONE);
			return false;
		}
		final ArrayList<Combatant> surroudings = state.getSurroundings(sameme);
		for (final Combatant c : new ArrayList<Combatant>(surroudings)) {
			if (!c.isAlly(sameme, state) || state.isengaged(c)) {
				surroudings.remove(c);
			}
		}
		if (surroudings.isEmpty()) {
			Game.message("No unthreatened allies nearby.", Delay.NONE);
			return false;
		}
		final Item item = UseItem.queryforitemselection(me, false);
		if (item == null) {
			return false;
		}
		String prompt = "Give this item to whom?\n";
		for (int i = 0; i < surroudings.size(); i++) {
			prompt += "[" + (i + 1) + "] " + surroudings.get(i) + "\n";
		}
		Game.message(prompt, Delay.NONE);
		try {
			final Combatant receiver = getmonster(surroudings.get(
					Integer.parseInt(InfoScreen.feedback().toString()) - 1));
			final Combatant giver = getmonster(me);
			receiver.ap += .5f;
			giver.ap += .5f;
			Javelin.app.fight.getbag(giver).remove(item);
			Javelin.app.fight.getbag(receiver).add(item);
		} catch (final NumberFormatException e) {
		} catch (final IndexOutOfBoundsException e) {
		}
		Game.messagepanel.clear();
		return true;
	}

	static Combatant getmonster(final Combatant receiverc) {
		for (final Combatant c : Fight.state.getCombatants()) {
			if (c.toString() == receiverc.toString()) {
				return c;
			}
		}
		return null;
	}
}
