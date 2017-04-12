package javelin.view.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import tyrant.mikera.engine.RPG;

/**
 * Squad selection screen when starting a new game.
 * 
 * @author alex
 */
public class SquadScreen extends InfoScreen {
	/** Minimum starting party encounter level. */
	public static final float INITIALEL = 5f;
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	static final int MONSTERPERPAGE = ALPHABET.indexOf('y');
	static final float[] SELECTABLE = { 1f, 1.25f };

	public static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (float cr : SELECTABLE) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (tier != null) {
				for (Monster candidate : tier) {
					String type = candidate.type.toLowerCase();
					if (!type.contains("undead")
							&& !type.contains("construct")) {
						CANDIDATES.add(candidate);
					}
				}
			}
		}
		Collections.sort(CANDIDATES, new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
	}

	ArrayList<Combatant> squad = new ArrayList<Combatant>();

	SquadScreen() {
		super("");
	}

	ArrayList<Combatant> select() {
		page(0);
		upgrade();
		return squad;
	}

	private void page(int index) {
		text = "Available monsters:\n";
		int next = index + MONSTERPERPAGE;
		int letter = printpage(index, next);
		Javelin.app.switchScreen(this);
		Character input = InfoScreen.feedback();
		if (input.equals(' ')) {
			page(next < CANDIDATES.size() ? next : 0);
		} else if (input == 'z') {
			fillwithrandom: while (!checkifsquadfull()) {
				Monster candidate = RPG.pick(CANDIDATES);
				for (Combatant m : squad) {
					if (m.source.name.equals(candidate.name)) {
						continue fillwithrandom;
					}
				}
				recruit(candidate);
			}
		} else if (input == '\n') {
			if (squad.isEmpty()) {
				page(index);
			}
		} else {
			int selection = ALPHABET.indexOf(input);
			if (selection >= 0 && selection < letter) {
				recruit(CANDIDATES.get(index + selection));
				if (checkifsquadfull()) {
					return;
				}
			}
			page(index);
		}
	}

	/**
	 * Adds {@link Commoner} levels to an understaffed squad or upgrades to
	 * level 6 as per {@link World#SCENARIO} rules.
	 */
	void upgrade() {
		if (World.SCENARIO) {
			ArrayList<Combatant> members = new ArrayList<Combatant>(squad);
			while (!members.isEmpty()) {
				ArrayList<Kit> kits = new ArrayList<Kit>(Kit.KITS);
				Collections.shuffle(kits);
				for (Kit k : kits) {
					Combatant c = members.get(0);
					if (Kit.getpossiblekits(c.source).contains(k)) {
						c.source.customName = Character.toUpperCase(
								k.name.charAt(0)) + k.name.substring(1);
						while (c.source.challengerating < 6) {
							c.upgrade(k.upgrades);
						}
						members.remove(0);
						if (members.isEmpty()) {
							return;
						}
					}
				}
			}
			return;
		}
		float startingcr = totalcr();
		while (ChallengeRatingCalculator.calculateel(squad) < INITIALEL) {
			ArrayList<Upgrade> u = new ArrayList<Upgrade>();
			u.add(Commoner.SINGLETON);
			Combatant.upgradeweakest(squad, u);
		}
		Squad.active.gold = RewardCalculator.getgold(totalcr() - startingcr);
	}

	private float totalcr() {
		int cr = 0;
		for (Combatant c : squad) {
			cr += ChallengeRatingCalculator.calculatecr(c.source);
		}
		return cr;
	}

	private void recruit(Monster m) {
		Combatant c = Javelin.recruit(m);
		c.hp = c.source.hd.maximize();
		c.maxhp = c.hp;
		squad.add(c);
		if (Javelin.DEBUG) {
			adddebugdata(c);
		}
	}

	boolean checkifsquadfull() {
		return World.SCENARIO ? squad.size() >= 4
				: ChallengeRatingCalculator.calculateel(squad) >= INITIALEL;
	}

	int printpage(int index, int next) {
		int letter = 0;
		for (int i = index; i < next && i < CANDIDATES.size(); i++) {
			text += "\n" + ALPHABET.charAt(letter) + " - "
					+ CANDIDATES.get(i).toString();
			letter += 1;
		}
		text += "\n";
		text += "\nPress letter to select character";
		if (CANDIDATES.size() > MONSTERPERPAGE) {
			text += "\nPress SPACE to switch pages";
		}
		text += "\nPress z for a random team";
		text += "\nPress ENTER to coninue with current selection";
		text += "\n";
		text += "\nYour team:";
		text += "\n";
		for (Combatant m : squad) {
			text += "\n" + m.source.toString();
		}
		return letter;
	}

	/** Start first squad in the morning */
	public static void open() {
		Squad.active = new Squad(0, 0, 8, null);
		new SquadScreen().select();
	}

	boolean first = true;

	void adddebugdata(Combatant c) {
	}
}
