package javelin.controller.db.reader;

import java.beans.PropertyVetoException;

import javelin.model.unit.Monster;

public class Speed extends FieldReader {
	public Speed(MonsterReader monsterReader, final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	void read(String value) throws PropertyVetoException {
		try {
			final int commentBegin = value.lastIndexOf("(");
			if (value.substring(commentBegin, value.lastIndexOf(")")).contains(
					",")) {
				value = value.substring(0, commentBegin).trim();
			}
		} catch (final StringIndexOutOfBoundsException e) {
			// doesn't have commentaries
		}

		final Monster m = reader.monster;
		for (String speedType : value.split(",")) {
			speedType = speedType.toLowerCase().replace(" feet.", " ft.")
					.replace(" ft.", "").trim();
			final int or = speedType.indexOf(" or");
			if (or != -1) {
				speedType = speedType.substring(0, or).trim();
			}
			if (speedType.contains("(") && !speedType.contains(" (")) {
				speedType = speedType.replaceAll("\\(", " \\(");
			}
			if (speedType.contains("climb ")) {
				// speed.setClimb(Long.parseLong(speedType.replace("climb ",
				// "")));
			} else if (speedType.contains("fly ")) {
				final String maneuverability = speedType.substring(
						speedType.indexOf("(") + 1, speedType.indexOf(")"));
				m.fly = Integer.parseInt(speedType.replace("fly ", "")
						.replace(" (" + maneuverability + ")", "").trim());
			} else if (speedType.contains("swim ")) {
				m.swim = Integer.parseInt(reader.cleanArmor(speedType).replace(
						"swim ", ""));
				// System.out.println(m.swim + " " + reader.monster.name
				// + " #swim");
			} else if (speedType.contains("burrow ")) {
				// speed.setBurrow(Long.parseLong(speedType.replace("burrow ",
				// "")));
			} else if (speedType.contains("base")) {
				// ignores base value
				continue;
			} else {
				m.walk = Integer.parseInt(reader.cleanArmor(speedType));
			}
		}
		if (m.fly == 0 && m.walk == 0) {
			reader.errorhandler.setInvalid("Cannot move out of water!");
		}
		if (m.fly > 0) {
			m.walk = 0;
		}
	}
}