package javelin.controller.action;

import java.util.ArrayList;

import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class GiveItem extends Action {
	public static final Action SINGLETON = new GiveItem();

	public GiveItem() {
		super("Give item to nearby ally (counts as action for both)", "g");
	}

	public static void give() {
		final BattleMap map = BattleScreen.active.map;
		final BattleState state = map.getState();
		final Combatant me = Game.hero().combatant;
		final Combatant sameme = state.translatecombatant(me);
		if (state.isEngaged(sameme)) {
			Game.message("You are engaged in combat!", null, Delay.NONE);
			return;
		}
		final ArrayList<Combatant> surroudings = state.getSurroudings(sameme);
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
			final Combatant receiver = getmonster(surroudings.get(Integer
					.parseInt(IntroScreen.feedback().toString()) - 1));
			final Combatant giver = getmonster(me);
			receiver.ap += .5f;
			giver.ap += .5f;
			Squad.active.equipment.get(giver.toString()).remove(item);
			Squad.active.equipment.get(receiver.toString()).add(item);
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
