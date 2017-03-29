package javelin.model.world.location.town;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.old.Game;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * Allows a player to recruit one type of unit.
 *
 * @author alex
 */
public class Dwelling extends Fortification {
	/**
	 * It would be cool to allow players to draft as well but this has a ton of
	 * implications, including balance ones.
	 *
	 * @author alex
	 */
	public class Draft extends Labor {
		Monster recruit;

		public Draft(Monster m) {
			super("Draft " + m.toString().toLowerCase(),
					Math.round(Math.max(1, m.challengerating)), null);
			recruit = m.clone();
		}

		@Override
		protected void define() {
			// nothing
		}

		@Override
		public void done() {
			town.garrison.add(new Combatant(recruit, true));
			if (ChallengeRatingCalculator
					.calculateel(town.garrison) > town.population) {
				MonsterGovernor.raid(town);
			}
		}

		@Override
		public boolean validate(District d) {
			return true;
		}

		@Override
		public void start() {
			super.start();
			volunteer = false;
		}
	}

	public Combatant dweller;
	/** A volunteer is willing to join a {@link Squad} permanently. */
	public boolean volunteer = false;

	/** Constructor. */
	public Dwelling() {
		this(null);
	}

	public Dwelling(Monster m) {
		super(null, null, 0, 0);
		if (m != null) {
			setdweller(m);
		}
		generate();
		generategarrison(0, 0);
		descriptionknown = dweller.toString() + " dwelling";
		descriptionunknown = "A dwelling";
	}

	@Override
	protected void generategarrison(int minel, int maxel) {
		if (dweller == null) {
			setdweller(RPG.pick(getcandidates(x, y)));
		}
		targetel = ChallengeRatingCalculator
				.crtoel(ChallengeRatingCalculator.calculatecr(dweller.source));
		gossip = dweller.source.intelligence > 8;
		for (int i = 0; i < 4; i++) {
			garrison.add(new Combatant(dweller.source.clone(), true));
		}
		generategarrison = false;
	}

	static public ArrayList<Monster> getcandidates(int x, int y) {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		monsters: for (Monster m : Javelin.ALLMONSTERS) {
			String terrain = Terrain.get(x, y).toString();
			if (m.getterrains().contains(terrain)) {
				candidates.add(m);
				continue monsters;
			}
		}
		return candidates;
	}

	void setdweller(Monster m) {
		dweller = new Combatant(m.clone(), true);
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
			Combatant mercenary = new Combatant(dweller.source.clone(), true);
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
		int xp = Math.round(dweller.source.challengerating * 100);
		if (!canbuy(xp)) {
			screen.print(
					"Cannot afford a " + monstertype + " (" + xp + "XP)...");
			Game.getInput();
			return true;
		}
		spend(dweller.source.challengerating);
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
					+ Math.round(100 * dweller.source.challengerating)
					+ "XP)\n";
		}
		text += "h - hire a " + monstertype + " mercenary ($"
				+ SelectScreen.formatcost(MercenariesGuild.getfee(dweller))
				+ "/day)\n";
		text += "p - pillage this dwelling ($"
				+ SelectScreen.formatcost(getspoils()) + ")\n";
		text += "q - quit\n";
		text += "\nCurrent gold: $" + SelectScreen.formatcost(Squad.active.gold)
				+ "\n";
		if (volunteer) {
			text += "Current XP: " + sumxp() + "XP\n";
		}
		return text;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		volunteer = volunteer || RPG.r(1, 7) == 1;
	}

	@Override
	public boolean isworking() {
		return !volunteer && !ishostile();
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	/**
	 * @return Total of XP between all active {@link Squad} members.
	 */
	public static int sumxp() {
		BigDecimal sum = new BigDecimal(0);
		for (Combatant c : Squad.active.members) {
			sum = sum.add(c.xp);
		}
		return Math.round(sum.floatValue() * 100);
	}

	/**
	 * @param price
	 *            Price in XP (100XP = 1CR).
	 * @return <code>true</code> if currently active {@link Squad} can afford
	 *         this much.
	 */
	static public boolean canbuy(double price) {
		return price <= sumxp();
	}

	/**
	 * @param cr
	 *            Spend this much CR in recruiting a rookie (1CR = 100XP).
	 */
	static public void spend(double cr) {
		double percapita = cr / new Float(Squad.active.members.size());
		boolean buyfromall = true;
		for (Combatant c : Squad.active.members) {
			if (percapita > c.xp.doubleValue()) {
				buyfromall = false;
				break;
			}
		}
		if (buyfromall) {
			for (Combatant c : Squad.active.members) {
				c.xp = c.xp.subtract(new BigDecimal(percapita));
			}
		} else {
			ArrayList<Combatant> squad = new ArrayList<Combatant>(
					Squad.active.members);
			ChallengeRatingCalculator.calculateel(squad);
			Collections.sort(squad, new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					final float cr1 = o2.xp.floatValue()
							+ o2.source.challengerating;
					final float cr2 = o1.xp.floatValue()
							+ o1.source.challengerating;
					return new Float(cr1).compareTo(cr2);
				}
			});
			for (Combatant c : squad) {
				if (c.xp.doubleValue() >= cr) {
					c.xp = c.xp.subtract(new BigDecimal(cr));
					return;
				}
				cr -= c.xp.doubleValue();
				c.xp = new BigDecimal(0);
			}
		}
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (d.town.ishostile() && volunteer) {
			upgrades.add(new Draft(dweller.source));
		}
		return upgrades;
	}
}
