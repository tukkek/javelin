package javelin.controller.quality;

import javelin.model.unit.Monster;

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
}
