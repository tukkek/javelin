package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class EnergyImmunity extends Quality {

	public EnergyImmunity() {
		super("immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		for (String type : EnergyResistance.RESISTANCETYPES) {
			if (declaration.contains(type)) {
				m.resistance = Integer.MAX_VALUE;
				return;
			}
		}
	}

	@Override
	public boolean has(Monster monster) {
		return monster.resistance == Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster) {
		return 5;
	}
}
