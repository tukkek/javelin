package javelin.view.screen;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.town.PurchaseScreen;

/**
 * Uses a {@link InfoScreen} to deal with succesful {@link Skills#diplomacy}
 * checks.
 * 
 * @author alex
 */
public class BribingScreen {
	/**
	 * @param foes
	 *            Opponents.
	 * @param dailyfee
	 *            For hiring as mercenary (requires passing the DC by 5 or
	 *            more).
	 * @param bribe
	 *            Price for getting rid of foes peacefully.
	 * @param canhire
	 *            If <code>false</code> will not show the option to hire as
	 *            mercenary.
	 * @return <code>false</code> in case a battle is started! Doesn't throw
	 *         {@link StartBattle}.
	 */
	public boolean bribe(List<Combatant> foes, int dailyfee, int bribe,
			boolean canhire) {
		String text = printdiplomacy(foes, dailyfee, bribe, canhire);
		InfoScreen screen = new InfoScreen("");
		while (true) {
			screen.print(text);
			int choice = InfoScreen.numberfeedback();
			if (choice == 1) {
				return false;
			}
			boolean nogold = false;
			if (choice == 2) {
				if (Squad.active.gold >= bribe) {
					Squad.active.gold -= bribe;
					return true;
				} else {
					nogold = true;
				}
			}
			if (canhire && choice == 3) {
				if (Squad.active.gold >= dailyfee) {
					for (Combatant foe : foes) {
						MercenariesGuild.recruit(foe, false);
					}
					return true;
				} else {
					nogold = true;
				}
			}
			if (nogold) {
				text += "\nNot enough gold!";
				screen.print(text);
			}
		}
	}

	static String printdiplomacy(List<Combatant> foes, int dailyfee, int bribe,
			boolean canhire) {
		String text = "You are able to parley with the "
				+ ChallengeCalculator.describedifficulty(foes) + " opponents!\n\n";
		text = Combatant.group(foes);
		text += "\n\nWhat do you want to do? You have $"
				+ PurchaseScreen.formatcost(Squad.active.gold) + ".";
		text += "\n";
		text += "\n1 - battle!";
		text += "\n2 - bribe them ($" + PurchaseScreen.formatcost(bribe) + ")";
		if (canhire) {
			text += "\n3 - hire as mercenaries ($"
					+ PurchaseScreen.formatcost(dailyfee) + "/day)";
		}
		text += "\n";
		return text;
	}
}
