package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * This is a tricky one because since we're treating all energy types as the
 * same one, and this is a high-rated special quality at 1cr per energy type, it
 * means that any monster with a immunity immediately is at least level 5 -
 * including, for example, a Small Skeleton.
 * 
 * Initially this was meant to flag {@link Monster#energyresistance} as
 * {@link Integer#MAX_VALUE} but to counter the problem above we're simply
 * adding the equivalent of 1CR as {@link EnergyResistance} instead.
 * 
 * Units can still have true immunity to all energy types through the
 * {@link EnergyImmunityUpgrade}.
 * 
 * @see EnergyResistance
 */
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
				m.energyresistance += Math.round(1 / EnergyResistance.CR);
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
