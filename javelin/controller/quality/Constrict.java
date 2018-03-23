package javelin.controller.quality;

import javelin.Javelin;
import javelin.controller.quality.resistance.EnergyResistance;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;

public class Constrict extends Quality {
	public Constrict() {
		super("constrict");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (Javelin.DEBUG && declaration.split(" ").length > 3) {
			throw new RuntimeException("#constrict malformed declaration for "
					+ m + ": " + declaration);
		}
		declaration = declaration.substring(declaration.indexOf(" ") + 1);
		int damageend = declaration.indexOf(" ");
		if (damageend < 0) {
			damageend = declaration.length();
		}
		String damage = declaration.substring(0, damageend);
		m.constrict = new javelin.model.unit.Constrict();
		m.constrict.damage = parsedamage(damage);
		for (String energy : EnergyResistance.ENERGYTYPES) {
			if (declaration.contains(energy)) {
				m.constrict.energy = true;
				break;
			}
		}
		m.addfeat(ImprovedGrapple.SINGLETON);
		// System.out.println(m + " " + m.constrict);
	}

	int parsedamage(String damage) {
		int d = damage.indexOf("d");
		int plus = damage.indexOf("+");
		if (plus < 0) {
			plus = damage.indexOf("-");
		}
		int end = damage.length();
		int die = Integer.parseInt(damage.substring(0, d));
		int faces = Integer
				.parseInt(damage.substring(d + 1, plus >= 0 ? plus : end));
		int bonus = plus > 0 ? Integer.parseInt(damage.substring(plus, end))
				: 0;
		return Math.max(1, Math.round(die * (faces + 1) / 2f + bonus));
	}

	@Override
	public boolean has(Monster m) {
		return m.constrict != null;
	}

	@Override
	public float rate(Monster m) {
		return m.constrict.damage * .05f;
	}

	@Override
	public String describe(Monster m) {
		return m.constrict.toString();
	}
}
