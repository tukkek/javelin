package javelin.view.screen;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Spawner;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.town.SelectScreen;

/**
 * Shows unit information.
 *
 * @author alex
 */
public class StatisticsScreen extends InfoScreen {
	/**
	 * @param c
	 *            Shows statistic for this unit.
	 */
	public StatisticsScreen(Combatant c) {
		super("");
		text = gettext(c, true);
		if (updatescreens().equals('v')) {
			text = "(The text below is taken from the d20 SRD and doesn't necessarily reflect the in-game enemy)\n\n"
					+ Javelin.DESCRIPTIONS.get(c.source.name);
			updatescreens();
		}
		Javelin.app.switchScreen(BattleScreen.active);
	}

	/**
	 * @param toggle
	 *            If <code>true</code> shows instruction on how to toggle more
	 *            information.
	 * @return Information for the given {@link Combatant}.
	 */
	@SuppressWarnings("deprecation")
	static public String gettext(Combatant c, boolean toggle) {
		Monster m = c.source;
		ArrayList<String> lines = new ArrayList<String>();
		String monstername = m.name;
		if (!m.group.isEmpty()) {
			monstername += " (" + m.group + ")";
		}
		lines.add(monstername);
		lines.add(capitalize(Monster.SIZES[m.size]) + " " + m.type);
		lines.add("");
		if (c.mercenary) {
			lines.add("Mercenary ($"
					+ SelectScreen.formatcost(MercenariesGuild.getfee(c))
					+ "/day)");
		}
		if (!(c.source instanceof Spawner)) {
			lines.add("Challenge rating "
					+ Math.round(CrCalculator.calculatecr(m)));
		}
		for (ClassLevelUpgrade classlevels : ClassLevelUpgrade.classes) {
			int level = classlevels.getlevel(m);
			if (level > 0) {
				lines.add(classlevels.descriptivename + " level " + level);
			}
		}
		lines.add(describealignment(m));
		lines.add("");
		lines.add(showhp(c, m));
		lines.add("Initiative   " + (m.initiative >= 0 ? "+" : "")
				+ m.initiative);
		lines.add("Speed        " + showspeed(m));
		lines.add("Armor class  " + alignnumber(m.ac + c.acmodifier));
		lines.add("");
		lines.add("Melee attacks");
		listattacks(lines, m.melee);
		lines.add("");
		lines.add("Ranged attacks");
		listattacks(lines, m.ranged);
		lines.add("");
		String qualities = describequalities(m, c);
		if (!qualities.isEmpty()) {
			lines.add(qualities);
		}
		lines.add("Saving throws");
		lines.add(" Fortitude   " + save(m.fort));
		lines.add(" Reflex      " + save(m.ref));
		lines.add(" Will        " + save(m.will));
		lines.add("");
		lines.add(printability(m.strength, "Strength"));
		lines.add(printability(m.dexterity, "Dexterity"));
		lines.add(printability(m.constitution, "Constitution"));
		lines.add(printability(m.intelligence, "Intelligence"));
		lines.add(printability(m.wisdom, "Wisdom"));
		lines.add(printability(m.charisma, "Charisma"));
		lines.add("");
		if (!m.feats.isEmpty()) {
			String feats = "Feats: ";
			for (javelin.model.unit.feat.Feat f : m.feats) {
				feats += f + ", ";
			}
			lines.add(feats.substring(0, feats.length() - 2) + ".");
			lines.add("");
		}
		final String skills = showskills(m);
		if (skills != null) {
			lines.add(skills);
		}
		if (toggle) {
			lines.add(
					"Press v to see the monster description, any other key to exit");
		}
		String text = "";
		for (String line : lines) {
			text += line + "\n";
		}
		return text;
	}

	static String showhp(Combatant c, Monster m) {
		boolean isally = Fight.state == null ? Squad.active.members.contains(c)
				: Fight.state.blueTeam.contains(c);
		final String hp;
		if (Javelin.DEBUG) {
			hp = Integer.toString(c.hp);
		} else if (isally) {
			hp = Integer.toString(c.maxhp);
		} else {
			hp = "~" + c.source.hd.average();
		}
		return "Hit dice     " + m.hd + " (" + hp + "hp)";
	}

	static String showspeed(Monster m) {
		long speed = m.fly;
		boolean fly = true;
		if (speed == 0) {
			fly = false;
			speed = m.walk;
		}
		String speedtext = alignnumber(speed) + " feet";
		if (fly) {
			speedtext += " flying";
		}
		long squares = speed / 5;
		speedtext += " (" + squares + " squares, "
				+ String.format("%.2f", ActionCost.MOVE / squares)
				+ "ap per square)";
		if (m.swim > 0) {
			speedtext += ", swim " + m.swim + " feet";
		}
		if (m.burrow > 0) {
			speedtext += ", burrow " + m.burrow + " feet";
		}
		return speedtext;
	}

	static String formatskill(String name, int ranks, int ability) {
		if (ranks == 0) {
			return "";
		}
		ranks += Monster.getbonus(ability);
		String bonus;
		if (ranks > 0) {
			bonus = "+" + ranks;
		} else {
			bonus = "-" + -ranks;
		}
		return name + " " + bonus + ", ";
	}

	@SuppressWarnings("deprecation")
	static String showskills(Monster m) {
		Skills s = m.skills;
		String output = "";
		output += formatskill("acrobatics", m.tumble(), m.dexterity);
		output += formatskill("concentration", m.concentrate(), m.constitution);
		output += formatskill("diplomacy", s.diplomacy, m.charisma);
		output += formatskill("disable device", s.disabledevice,
				m.intelligence);
		output += formatskill("disguise", s.disguise(m) - 10, m.charisma);
		output += formatskill("gather information", s.gatherinformation,
				m.charisma);
		output += formatskill("heal", s.heal, m.wisdom);
		output += formatskill("knowledge", s.knowledge, m.intelligence);
		output += formatskill("perception", s.perception, m.wisdom);
		output += formatskill("search", s.search, m.intelligence);
		output += formatskill("spellcraft", s.spellcraft, m.intelligence);
		output += formatskill("stealth", s.stealth, m.dexterity);
		output += formatskill("survival", s.survival, m.wisdom);
		output += formatskill("use magic device", s.usemagicdevice, m.charisma);
		return output.isEmpty() ? null
				: "Skills: " + output.substring(0, output.length() - 2) + ".\n";
	}

	static String describealignment(Monster m) {
		String alignment;
		if (m.lawful == null) {
			if (m.good == null) {
				return "True neutral";
			}
			alignment = "Neutral";
		} else if (m.lawful) {
			alignment = "Lawful";
		} else {
			alignment = "Chaotic";
		}
		if (m.good == null) {
			return alignment += " neutral";
		} else if (m.good) {
			return alignment + " good";
		} else {
			return alignment + " evil";
		}
	}

	private static String save(int x) {
		String sign = "";
		if (x >= 0) {
			sign = "+";
		}
		return sign + x;
	}

	static private String printability(int score, String abilityname) {
		abilityname += " ";
		while (abilityname.length() < 13) {
			abilityname += " ";
		}
		return abilityname + alignnumber(score) + " ("
				+ Monster.getsignedbonus(score) + ")";
	}

	private static String describequalities(Monster m, Combatant c) {
		String s = printqualities("Maneuvers", c.disciplines.getmaneuvers());
		ArrayList<String> spells = new ArrayList<String>(c.spells.size());
		for (Spell spell : c.spells) {
			spells.add(spell.toString());
		}
		s += printqualities("Spells", spells);
		ArrayList<String> attacks = new ArrayList<String>();
		for (BreathWeapon breath : m.breaths) {
			attacks.add(breath.toString());
		}
		if (m.touch != null) {
			attacks.add(m.touch.toString());
		}
		s += printqualities("Special attacks", attacks);
		ArrayList<String> qualities = new ArrayList<String>();
		for (Quality q : Quality.qualities) {
			if (q.has(m)) {
				String description = q.describe(m);
				if (description != null) {
					qualities.add(description);
				}
			}
		}
		s += printqualities("Special qualities", qualities);
		if (!s.isEmpty()) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	static String printqualities(String header, ArrayList<?> qualities) {
		if (qualities.isEmpty()) {
			return "";
		}
		header += ": ";
		qualities.sort(null);
		for (Object quality : qualities) {
			header += quality.toString().toLowerCase() + ", ";
		}
		return header.substring(0, header.length() - 2) + ".\n\n";
	}

	static void listattacks(ArrayList<String> lines,
			List<AttackSequence> melee) {
		if (melee.isEmpty()) {
			lines.add(" None");
			return;
		}
		for (AttackSequence sequence : melee) {
			lines.add(" " + sequence.toString());
		}
	}

	/**
	 * @return Given {@link String} with only first {@link Character} as
	 *         uppercase.
	 */
	public static String capitalize(String size) {
		return Character.toUpperCase(size.charAt(0))
				+ size.substring(1).toLowerCase();
	}

	private static String alignnumber(long score) {
		return score < 10 ? " " + score : Long.toString(score);
	}

	Character updatescreens() {
		Javelin.app.switchScreen(this);
		return InfoScreen.feedback();
	}
}
