package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.old.Game;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.PurchaseScreen;
import javelin.view.screen.town.RecruitScreen;
import tyrant.mikera.engine.RPG;

/**
 * Allows a player to recruit one type of unit.
 * 
 * @author alex
 */
public class Dwelling extends Fortification {
	private Combatant dweller;
	/** A volunteer is willing to join a {@link Squad} permanently. */
	public boolean volunteer = false;

	/** Constructor. */
	public Dwelling() {
		super(null, null, 0, 0);
		generate();
		generategarrison(0, 0);
		descriptionknown = dweller.toString() + " dwelling";
		descriptionunknown = "A dwelling";
	}

	@Override
	protected void generategarrison(int minel, int maxel) {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		monsters: for (Monster m : Javelin.ALLMONSTERS) {
			String terrain = Terrain.get(x, y).toString();
			if (m.getterrains().contains(terrain)) {
				candidates.add(m);
				continue monsters;
			}
		}
		dweller = new Combatant(null, RPG.pick(candidates).clone(), true);
		targetel = ChallengeRatingCalculator.elFromCr(
				ChallengeRatingCalculator.calculateCr(dweller.source));
		gossip = dweller.source.intelligence > 8;
		for (int i = 0; i < 4; i++) {
			garrison.add(new Combatant(null, dweller.source.clone(), true));
		}
		generategarrison = false;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String monstertype = dweller.toString().toLowerCase();
		InfoScreen screen = new InfoScreen("");
		screen.print(prompt(monstertype));
		char choice = ' ';
		List<Character> options = Arrays.asList('d', 'h', 'p', 'q');
		while (!options.contains(choice)) {
			choice = InfoScreen.feedback();
		}
		if (choice == 'q') {
			return true;
		}
		if (choice == 'h') {
			Combatant mercenary =
					new Combatant(null, dweller.source.clone(), true);
			mercenary.mercenary = true;
			MercenariesGuild.recruit(mercenary, true);
			return true;
		}
		if (choice == 'p') {
			pillage();
			return true;
		}
		if (!volunteer) {
			return true;
		}
		int xp = Math.round(dweller.source.challengeRating * 100);
		if (!RecruitScreen.canbuy(xp)) {
			screen.print(
					"Cannot afford a " + monstertype + " (" + xp + "XP)...");
			Game.getInput();
			return true;
		}
		RecruitScreen.spend(dweller.source.challengeRating);
		Javelin.recruit(dweller.source.clone());
		volunteer = false;
		return true;
	}

	String prompt(String monstertype) {
		String text = "You enter a " + monstertype + " dwelling.\n";
		if (volunteer) {
			text += "A volunteer is available to join you.\n\n";
		} else {
			text += "No volunteer is available right now but you can still hire mercenaries.\n\n";
		}
		text += "What do you want to do?\n\n";
		if (volunteer) {
			text += "d - draft a volunteer ("
					+ Math.round(100 * dweller.source.challengeRating)
					+ "XP)\n";
		}
		text += "h - hire a " + monstertype + " mercenary ($"
				+ PurchaseScreen.formatcost(MercenariesGuild.getfee(dweller))
				+ "/day)\n";
		text += "p - pillage this dwelling ($"
				+ PurchaseScreen.formatcost(getspoils()) + ")\n";
		text += "q - quit\n";
		text += "\nCurrent gold: $"
				+ PurchaseScreen.formatcost(Squad.active.gold) + "\n";
		if (volunteer) {
			text += "Current XP: " + RecruitScreen.sumxp() + "XP\n";
		}
		return text;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		volunteer = volunteer || RPG.r(1, 7) == 1;
	}

	@Override
	public boolean hasupgraded() {
		return volunteer;
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}
}
