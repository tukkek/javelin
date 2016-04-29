package javelin.controller.db.reader.factor;

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
			if (skill.contains("tumble")) {
				m.skills.acrobatics = value - Monster.getbonus(m.dexterity);
			} else if (skill.contains("concentration")) {
				m.skills.concentration =
						value - Monster.getbonus(m.constitution);
			} else if (skill.contains("diplomacy")) {
				m.skills.diplomacy = value - Monster.getbonus(m.charisma);
			} else if (skill.contains("disable device")) {
				m.skills.disabledevice =
						value - Monster.getbonus(m.intelligence);
			} else if (skill.contains("gather information")) {
				m.skills.gatherinformation =
						value - Monster.getbonus(m.charisma);
			} else if (skill.contains("hide")) {
				m.skills.hide = value - Monster.getbonus(m.dexterity);
			} else if (skill.contains("knowledge")) {
				int knowledge = value - Monster.getbonus(m.intelligence);
				if (knowledge > m.skills.knowledge) {
					m.skills.knowledge = knowledge;
				}
			} else if (skill.contains("listen")) {
				m.skills.listen = value - Monster.getbonus(m.wisdom);
			} else if (skill.contains("move silently")) {
				m.skills.movesilently = value - Monster.getbonus(m.dexterity);
			} else if (skill.contains("search")) {
				m.skills.search = value - Monster.getbonus(m.intelligence);
			} else if (skill.contains("spellcraft")) {
				m.skills.spellcraft = value - Monster.getbonus(m.intelligence);
			} else if (skill.contains("spot")) {
				m.skills.spot = value - Monster.getbonus(m.wisdom);
			} else if (skill.contains("survival")) {
				m.skills.survival = value - Monster.getbonus(m.wisdom);
			} else if (Javelin.DEBUG) {
				UNKNOWN.add(skill.replace('-', '+').split("\\+")[0].trim());
			}
		}
	}
}
