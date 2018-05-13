package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

/**
 * Input method for {@link Monster#skills}. Calculates ranks, not total bonus.
 *
 * @author alex
 */
public class Skills extends FieldReader {
	/** Keeps track of unknown skills for debugging purposes. */
	public static final HashSet<String> UNKNOWN = Javelin.DEBUG
			? new HashSet<String>() : null;

	/** Constructor. */
	public Skills(MonsterReader reader, String fieldname) {
		super(reader, fieldname);
	}

	@Override
	public void read(String text)
			throws NumberFormatException, PropertyVetoException {
		Monster m = reader.monster;
		for (String skill : text.split(",")) {
			skill = skill.trim().toLowerCase();
			String[] split = skill.split(" ");
			int value = Integer
					.parseInt(split[split.length - 1].replace("*", ""));
			apply(m, skill, value);
		}
	}

	static void apply(Monster m, String skill, int value) {
		skill = skill.substring(0, skill.lastIndexOf(" "));
		while (skill.contains("(")) {
			String detail = skill.substring(skill.indexOf("("),
					skill.indexOf(")") + 1);
			skill = skill.replace(detail, "");
		}
		while (skill.contains("  ")) {
			skill = skill.replace("  ", " ");
		}
		skill = skill.trim();
		Skill s = Skill.BYNAME.get(skill);
		if (s != null) {
			s.setoriginal(value, m);
		} else if (Javelin.DEBUG) {
			UNKNOWN.add(skill);
		}
	}
}
