package javelin.controller.action;

import java.util.ArrayList;

import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Hands item over to a friendly unit.
 * 
 * @author alex
 */
public class PassItem extends Action {
	public static final Action SINGLETON = new PassItem();

	public PassItem() {
		super("Pass item to nearby ally (counts as action for both)", "p");
	}

	public static void give() {
		final BattleMap map = BattleScreen.active.map;
		final BattleState state = map.getState();
		final Combatant me = Game.hero().combatant;
		final Combatant sameme = state.clone(me);
		if (state.isEngaged(sameme)) {
			Game.message("You are engaged in combat!", null, Delay.NONE);
			return;
		}
		final ArrayList<Combatant> surroudings = state.getSurroundings(sameme);
		for (final Combatant c : new ArrayList<Combatant>(surroudings)) {
			if (!c.isAlly(sameme, state) || state.isEngaged(c)) {
				surroudings.remove(c);
			}
		}
		if (surroudings.isEmpty()) {
			Game.message("No unthreatened allies nearby.", null, Delay.NONE);
			return;
		}
		final Item item = UseItem.queryforitemselection(me);
		if (item == null) {
			return;
		}
		String prompt = "Give this item to whom?\n";
		for (int i = 0; i < surroudings.size(); i++) {
			prompt += "[" + (i + 1) + "] " + surroudings.get(i) + "\n";
		}
		Game.message(prompt, null, Delay.NONE);
		try {
			final Combatant receiver = getmonster(surroudings.get(
					Integer.parseInt(InfoScreen.feedback().toString()) - 1));
			final Combatant giver = getmonster(me);
			receiver.ap += .5f;
			giver.ap += .5f;
			Squad.active.equipment.get(giver.id).remove(item);
			Squad.active.equipment.get(receiver.id).add(item);
		} catch (final NumberFormatException e) {
		} catch (final IndexOutOfBoundsException e) {
		}
		Game.messagepanel.clear();
	}

	public static Combatant getmonster(final Combatant receiverc) {
		for (final Combatant c : BattleMap.combatants) {
			if (c.toString() == receiverc.toString()) {
				return c;
			}
		}
		return null;
	}
}
