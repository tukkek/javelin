package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 * 
 * Tries to treat all energies as a single resistance.
 * 
 * They should be .02CR but currently the game assume 1 point of energy
 * resistance is actually 5 (one for each type of energy resistance).
 * 
 * @author alex
 * @see javelin.controller.quality.EnergyResistance#RESISTANCETYPES
 * @see Monster#resistance
 */
public class EnergyResistance extends Quality {
	static final String[] RESISTANCETYPES =
			new String[] { "cold", "fire", "acid", "electricity", "sonic" };
	private static final String[] BLACKLIST = new String[] { "turn resistance",
			"resistance to ranged attacks", "charm resistance" };

	public EnergyResistance() {
		super("resistance");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (m.resistance == Integer.MAX_VALUE) {
			return;
		}
		declaration = declaration.toLowerCase();
		for (String ignore : BLACKLIST) {
			if (declaration.contains(ignore)) {
				return;
			}
		}
		float amount = Integer.parseInt(
				declaration.substring(declaration.lastIndexOf(' ') + 1));
		float types = 0;
		for (String type : RESISTANCETYPES) {
			if (declaration.contains(type)) {
				types += 1f;
			}
		}
		m.resistance = Math.round(amount * types / 5f);
	}

	@Override
	public boolean has(Monster monster) {
		return 0 < monster.resistance && monster.resistance < Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster) {
		return monster.resistance * .02f * 5;
	}
}
