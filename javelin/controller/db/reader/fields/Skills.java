package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;

/**
 * Input method for {@link Monster#skills}. Calculates ranks, not total bonus.
 * 
 * @author alex
 */
public class Skills extends FieldReader {
	/** Keeps track of unknown skills for debugging purposes. */
	public static final HashSet<String> UNKNOWN =
			Javelin.DEBUG ? new HashSet<String>() : null;

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
			int value =
					Integer.parseInt(split[split.length - 1].replace("*", ""));
			apply(m, skill, value);
		}
	}

	static void apply(Monster m, String skill, int value) {
		final int dexbased = value - Monster.getbonus(m.dexterity);
		final int conbased = value - Monster.getbonus(m.constitution);
		final int chabased = value - Monster.getbonus(m.charisma);
		final int intbased = value - Monster.getbonus(m.intelligence);
		final int wisbased = value - Monster.getbonus(m.wisdom);
		javelin.model.unit.Skills s = m.skills;
		if (skill.contains("tumble")) {
			s.acrobatics = Math.max(s.acrobatics, dexbased);
		} else if (skill.contains("concentration")) {
			s.concentration = Math.max(s.concentration, conbased);
		} else if (skill.contains("diplomacy")) {
			s.diplomacy = Math.max(s.diplomacy, chabased);
		} else if (skill.contains("disable device")) {
			s.disabledevice = Math.max(s.disabledevice, intbased);
		} else if (skill.contains("gather information")) {
			s.gatherinformation = Math.max(s.gatherinformation, chabased);
		} else if (skill.contains("hide")) {
			s.stealth = Math.max(s.stealth, dexbased);
		} else if (skill.contains("move silently")) {
			s.stealth = Math.max(s.stealth, dexbased);
		} else if (skill.contains("listen")) {
			s.perception = Math.max(s.perception, wisbased);
		} else if (skill.contains("spot")) {
			s.perception = Math.max(s.perception, wisbased);
		} else if (skill.contains("knowledge")) {
			s.knowledge = Math.max(s.knowledge, intbased);
		} else if (skill.contains("search")) {
			s.search = Math.max(s.search, intbased);
		} else if (skill.contains("spellcraft")) {
			s.spellcraft = Math.max(s.spellcraft, intbased);
		} else if (skill.contains("survival")) {
			s.survival = Math.max(s.survival, wisbased);
		} else if (skill.contains("use magic device")) {
			s.usemagicdevice = Math.max(s.usemagicdevice, chabased);
		} else if (skill.contains("heal")) {
			s.heal = Math.max(s.heal, wisbased);
		} else if (skill.contains("disguise")) {
			s.disguise = Math.max(s.disguise, chabased);
		} else if (Javelin.DEBUG) {
			UNKNOWN.add(skill.replace('-', '+').split("\\+")[0].trim());
		}
	}
}
