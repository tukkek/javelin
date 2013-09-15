package javelin.view.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.BreathWeapon;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.InfoScreen;

public class StatisticsScreen extends InfoScreen {
	public StatisticsScreen(Combatant c) {
		super("");
		Monster m = c.source;
		ArrayList<String> lines = new ArrayList<String>();
		String monstername = m.name;
		if (!m.group.isEmpty()) {
			monstername += " (" + m.group + ")";
		}
		lines.add(monstername + "\n");
		lines.add(capitalize(Monster.SIZES[m.size]) + " " + m.monsterType);
		lines.add("Challenge rating "
				+ Math.round(ChallengeRatingCalculator.calculateCr(m)));
		for (ClassAdvancement classlevels : ClassAdvancement.CLASSES) {
			int level = classlevels.getlevel(m);
			if (level > 0) {
				lines.add(classlevels.descriptivename + " level " + level);
			}
		}
		long speed = m.fly;
		boolean fly = true;
		if (speed == 0) {
			fly = false;
			speed = m.walk;
		}
		String speedtext = alignnumber(speed) + " feet";
		if (fly) {
			speedtext += " (flying)";
		}
		if (m.swim > 0) {
			speedtext += ", swim " + m.swim + " feet";
		}
		lines.add("");
		lines.add("Hit dice     " + m.hd.toString());
		lines.add("Initiative   " + alignnumber(m.initiative));
		lines.add("Speed        " + speedtext);
		lines.add("Armor class  " + alignnumber(m.ac));
		lines.add("");
		lines.add("Mêlée attacks");
		listattacks(lines, m.melee);
		lines.add("");
		lines.add("Ranged attacks");
		listattacks(lines, m.ranged);
		lines.add("");
		lines.add(describequalities(m, c));
		lines.add("");
		lines.add("Saving throwns");
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
		String feats = "Feats: ";
		if (m.feats.isEmpty()) {
			feats += "none";
		} else {
			for (javelin.model.feat.Feat f : m.feats) {
				feats += f.name + ", ";
			}
			feats = feats.substring(0, feats.length() - 2);
		}
		lines.add(feats);
		lines.add("");
		lines.add("Press v to see the monster description, any other key to exit");
		for (String line : lines) {
			text += line + "\n";
		}
		if (updatescreens().equals('v')) {
			text = "(The text below is taken from the d20 SRD and doesn't necessarily reflect the in-game enemy)\n\n"
					+ Javelin.DESCRIPTIONS.get(m.name);
			updatescreens();
		}
		Javelin.app.switchScreen(BattleScreen.active);
	}

	private String save(int x) {
		String sign = "";
		if (x >= 0) {
			sign = "+";
		}
		return sign + x;
	}

	private String printability(int score, String abilityname) {
		abilityname += " ";
		while (abilityname.length() < 13) {
			abilityname += " ";
		}
		int bonus = Monster.getbonus(score);
		return abilityname + (bonus >= 0 ? "+" : "") + bonus;
	}

	private String describequalities(Monster m, Combatant c) {
		String string = "Special qualitites: ";
		ArrayList<String> qualities = new ArrayList<String>();
		if (m.fasthealing > 0) {
			qualities.add("fast healing " + m.fasthealing);
		}
		HashSet<String> spells = new HashSet<String>();
		for (Spell s : c.spells) {
			if (spells.add(s.name)) {
				qualities.add(s.toString());
			}
		}
		if (m.vision == 1) {
			qualities.add("low-light vision");
		} else if (m.vision == 2) {
			qualities.add("darkvision");
		}
		for (BreathWeapon breath : m.breaths) {
			qualities.add(breath.toString());
		}
		if (m.dr > 0) {
			qualities.add("damage reduction " + m.dr);
		}
		if (m.resistance == Integer.MAX_VALUE) {
			qualities.add("energy immunity");
		} else if (m.resistance != 0) {
			qualities.add("energy resistance " + m.resistance);
		}
		if (m.sr == Integer.MAX_VALUE) {
			qualities.add("spell immunity");
		} else if (m.sr != 0) {
			qualities.add("spell resistance " + m.sr);
		}
		if (qualities.isEmpty()) {
			qualities.add("none");
		}
		for (String quality : qualities) {
			string += quality.toLowerCase() + ", ";
		}
		return string.substring(0, string.length() - 2);
	}

	public void listattacks(ArrayList<String> lines, List<AttackSequence> melee) {
		if (melee.isEmpty()) {
			lines.add(" None");
			return;
		}
		for (AttackSequence sequence : melee) {
			lines.add(" " + sequence.toString());
		}
	}

	public static String capitalize(String size) {
		return Character.toUpperCase(size.charAt(0))
				+ size.substring(1).toLowerCase();
	}

	private String alignnumber(long score) {
		return score < 10 ? " " + score : Long.toString(score);
	}

	public Character updatescreens() {
		Javelin.app.switchScreen(this);
		return IntroScreen.feedback();
	}
}
