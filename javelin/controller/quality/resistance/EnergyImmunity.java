package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/** @see EnergyResistance */
public class EnergyImmunity extends Quality {
	/**
	 * See the d20 SRD for more info.
	 */
	static class EnergyImmunityUpgrade extends Upgrade {

		public EnergyImmunityUpgrade() {
			super("Energy immunity");
		}

		@Override
		public String inform(Combatant m) {
			return "";
		}

		@Override
		public boolean apply(Combatant m) {
			if (m.source.energyresistance == Integer.MAX_VALUE) {
				return false;
			}
			m.source.energyresistance = Integer.MAX_VALUE;
			return true;
		}
	}

	public EnergyImmunity() {
		super("Energy immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		for (String type : EnergyResistance.RESISTANCETYPES) {
			if (declaration.contains(type)) {
				m.energyresistance = Integer.MAX_VALUE;
				return;
			}
		}
	}

	@Override
	public boolean has(Monster monster) {
		return monster.energyresistance == Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster) {
		return 1 * 5;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new EnergyImmunityUpgrade());
	}

	@Override
	public boolean apply(String attack, Monster m) {
		if (!attack.contains("immunity")) {
			return false;
		}
		return attack.contains("cold") || attack.contains("electricity")
				|| attack.contains("fire") || attack.contains("acid");
	}
}
