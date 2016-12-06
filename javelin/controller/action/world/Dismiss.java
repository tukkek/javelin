package javelin.controller.action.world;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.WorldScreen;

/**
 * Allows members to be removed from a {@link Squad} at any point.
 * 
 * @author alex
 */
public class Dismiss extends WorldAction {
	/** Constructor. */
	public Dismiss() {
		super("Dismiss squad member", new int[] { 'D' }, new String[] { "D" });
	}

	@Override
	public void perform(WorldScreen screen) {
		ArrayList<String> members =
				new ArrayList<String>(Squad.active.members.size());
		for (Combatant c : Squad.active.members) {
			members.add(
					c.mercenary ? c.toString() + " (mercenary)" : c.toString());
		}
		int choice =
				Javelin.choose("Which squad member do you want to dismiss?",
						members, true, false);
		if (choice == -1) {
			return;
		}
		Combatant chosen = Squad.active.members.get(choice);
		float dailyupkeep = chosen.source.size() / 2f;
		if (chosen.mercenary) {
			dailyupkeep += MercenariesGuild.getfee(chosen);
		}
		ArrayList<String> confirm = new ArrayList<String>(2);
		confirm.add("Yes, I am sure.");
		confirm.add("Not really, keep unit.");
		if (Javelin.choose("Are you sure you want to dismiss " + chosen
				+ ", with a daily cost of $" + Math.round(dailyupkeep) + "?",
				confirm, true, true) == 0) {
			Squad.active.dismiss(chosen);
		}
	}
}
