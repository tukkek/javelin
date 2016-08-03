package javelin.model.world.location.unique;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Heal;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.Spellcraft;
import javelin.controller.upgrade.skill.Stealth;
import javelin.controller.upgrade.skill.Survival;
import javelin.controller.upgrade.skill.UseMagicDevice;
import javelin.model.spell.Summon;
import javelin.model.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.spell.evocation.MagicMissile;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

/**
 * Applies class kits to low-level player units. A kit is usually a set of 3 to
 * 5 upgrades that can be applied randomly to ensure progression up to level
 * challenge rating 2.
 * 
 * @author alex
 */
public class AdventurersGuild extends UniqueLocation {

	private static final String TITLE = "Adventurers guild";

	static final String NONE = "don't study";
	static final String FIGHTER = "fighter";
	static final String ROGUE = "rogue";
	static final String DRUID = "druid";
	static final String WIZARD = "wizard";
	static final String CLERIC = "cleric";
	static final String BARD = "bard";

	private static final float TARGETLEVEL = 2;
	static final String[] TITLES =
			new String[] { "underlings", "teachers", "masters", "legends" };
	static final ArrayList<String> KITS = new ArrayList<String>();
	static final TreeMap<String, List<Upgrade>> UPGRADETREE =
			new TreeMap<String, List<Upgrade>>();

	static {
		for (String kit : new String[] { BARD, CLERIC, DRUID, FIGHTER, ROGUE,
				WIZARD, NONE }) {
			KITS.add(kit);
		}
		ArrayList<Upgrade> list = new ArrayList<Upgrade>();
		UPGRADETREE.put(FIGHTER, list);
		list.add(Warrior.SINGLETON);
		list.add(new MeleeDamage());
		list.add(new RaiseStrength());
		list = new ArrayList<Upgrade>();
		UPGRADETREE.put(ROGUE, list);
		list.add(Expert.SINGLETON);
		list.add(new RaiseDexterity());
		list.add(Acrobatics.SINGLETON);
		list.add(DisableDevice.SINGLETON);
		list.add(Stealth.SINGLETON);
		list = new ArrayList<Upgrade>();
		UPGRADETREE.put(WIZARD, list);
		list.add(Aristocrat.SINGLETON);
		list.add(new MagicMissile());
		list.add(Concentration.SINGLETON);
		list.add(Spellcraft.SINGLETON);
		list = new ArrayList<Upgrade>();
		UPGRADETREE.put(CLERIC, list);
		list.add(Aristocrat.SINGLETON);
		list.add(new CureModerateWounds());
		list.add(new RaiseWisdom());
		list.add(Knowledge.SINGLETON);
		list.add(Heal.SINGLETON);
		list = new ArrayList<Upgrade>();
		UPGRADETREE.put(DRUID, list);
		list.add(Commoner.SINGLETON);
		list.add(new Summon("Small monstrous centipede", 1));
		list.add(new Summon("Dire rat", 1));
		list.add(new Summon("Eagle", 1));
		list.add(Survival.SINGLETON);
		list = new ArrayList<Upgrade>();
		UPGRADETREE.put(BARD, list);
		list.add(Commoner.SINGLETON);
		list.add(Diplomacy.SINGLETON);
		list.add(GatherInformation.SINGLETON);
		list.add(Knowledge.SINGLETON);
		list.add(UseMagicDevice.SINGLETON);
	}

	List<String> courses = null;

	/** Constructor. */
	public AdventurersGuild() {
		super(TITLE, TITLE, 1, 1);
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// clear
	}

	@Override
	public boolean interact() {
		courses = null;
		ArrayList<Combatant> students = new ArrayList<Combatant>();
		float mostpowerful = 0;
		for (Combatant c : Squad.active.members) {
			if (c.mercenary) {
				students.clear();
				break;
			}
			float cr = ChallengeRatingCalculator.calculateCr(c.source);
			if (cr < TARGETLEVEL && c.xp.floatValue() >= 0) {
				students.add(c);
			}
			mostpowerful = Math.max(mostpowerful, cr);
		}
		float pay = pay(rank(mostpowerful), 30, Squad.active.members);
		InfoScreen screen = new InfoScreen("");
		Character input = 'a';
		while (input != 'q') {
			screen.print(show(students, mostpowerful, pay));
			input = screen.getInput();
			int index = getindex(input, students);
			if (0 <= index && index < students.size()) {
				change(index);
				continue;
			}
			if (input == 'w') {
				Squad.active.hourselapsed += 24 * 30;
				Squad.active.gold += Math.round(pay);
				return true;
			}
			if (input == 't') {
				train(students);
				return true;
			}
		}
		return true;
	}

	void change(int index) {
		final int from = KITS.indexOf(courses.get(index));
		int to = from + 1;
		if (to >= KITS.size()) {
			to = 0;
		}
		courses.set(index, KITS.get(to));
	}

	private int getindex(Character input, ArrayList<Combatant> students) {
		try {
			return Integer.parseInt(input.toString()) - 1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	String show(ArrayList<Combatant> students, float mostpowerful, float pay) {
		String t =
				"You enter a tall building where people are training and studying in several large rooms.\n\n";
		if (!students.isEmpty()) {
			while (students.size() >= 10) {
				students.remove(0);
			}
			if (courses == null) {
				courses = new ArrayList<String>(students.size());
				for (int i = 0; i < students.size(); i++) {
					courses.add(determinedefaultkit(students.get(i)));
				}
			}
			for (int i = 0; i < students.size(); i++) {
				t += (i + 1) + " - " + students.get(i) + " (" + courses.get(i)
						+ ")\n";
			}
			t += "\nPress the respective number to change careers.\n\n";
			t += "t - begin training\n";
		}
		t += "w - work as " + TITLES[rank(mostpowerful)] + " for a month ($"
				+ PurchaseScreen.formatcost(Math.round(pay))
				+ " minus expenses)";
		t += "\nq - quit";
		return t;
	}

	String determinedefaultkit(Combatant combatant) {
		int strength = combatant.source.strength;
		int dexterity = combatant.source.dexterity;
		int constitution = combatant.source.constitution;
		int intelligence = combatant.source.intelligence;
		int wisdom = combatant.source.wisdom;
		int charisma = combatant.source.charisma;
		int max = Math.max(strength, dexterity);
		max = Math.max(max, constitution);
		max = Math.max(max, intelligence);
		max = Math.max(max, wisdom);
		max = Math.max(max, charisma);
		if (max == strength) {
			return FIGHTER;
		} else if (max == dexterity) {
			return ROGUE;
		} else if (max == intelligence) {
			return WIZARD;
		} else if (max == wisdom) {
			return CLERIC;
		} else if (max == constitution) {
			return DRUID;
		} else if (max == charisma) {
			return BARD;
		}
		return FIGHTER;
	}

	void train(ArrayList<Combatant> students) {
		Squad.active.hourselapsed += 24 * 7;
		for (int i = 0; i < students.size(); i++) {
			String kit = courses.get(i);
			if (kit == NONE) {
				continue;
			}
			Combatant student = students.get(i);
			String training = student + " learns:\n\n";
			float cr = ChallengeRatingCalculator.calculateCr(student.source);
			float original = cr;
			Upgrade purchaseskills = null;
			while (cr < TARGETLEVEL) {
				Upgrade u = RPG.pick(UPGRADETREE.get(kit));
				if (u.upgrade(student)) {
					training += u.name + "\n";
				}
				cr = ChallengeRatingCalculator.calculateCr(student.source);
				if (u.purchaseskills) {
					purchaseskills = u;
				}
			}
			student.xp = student.xp.subtract(new BigDecimal(cr - original));
			training += "\nPress ENTER to continue...";
			InfoScreen screen = new InfoScreen(training);
			Javelin.app.switchScreen(screen);
			while (screen.getInput() != '\n') {
				// wait
			}
			if (purchaseskills != null) {
				student.source.purchaseskills(purchaseskills).show();
			}
		}
	}

	/**
	 * Calculates a paycheck that is twice the food cost for a character per
	 * day.
	 * 
	 * @param bonus
	 *            Each unit here means an entire dayworth of food bonus to the
	 *            paycheck per day.
	 * @param days
	 *            This method does not update {@link Squad#hourselapsed}.
	 * @return Amount in gold pieces ($).
	 */
	public static float pay(int bonus, float days, List<Combatant> workers) {
		float pay = 0;
		for (Combatant c : workers) {
			pay += c.source.eat() * (2 + bonus) * days;
		}
		return pay;
	}

	int rank(float cr) {
		if (cr < 5) {
			return 0;
		}
		if (cr < 10) {
			return 1;
		}
		if (cr < 15) {
			return 2;
		}
		return 3;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}
}
