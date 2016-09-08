package javelin.controller.quality;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class FastHealing extends Quality {
	/**
	 * See the d20 SRD for more info.
	 */
	static class FastHealingUpgrade extends Upgrade {
		public FastHealingUpgrade() {
			super("Fast healing");
		}

		@Override
		public String inform(final Combatant m) {
			return "Current: " + m.source.fasthealing + " ("
					+ Math.round(100 * m.source.fasthealing / m.maxhp) + "%)";
		}

		@Override
		public boolean apply(final Combatant m) {
			int heal = m.source.hd.count();
			if (m.source.fasthealing >= heal) {
				// design parameter (fast healing + regeneration)
				return false;
			}
			m.source.fasthealing = heal;
			return true;
		}

	}

	public FastHealing(String name) {
		super(name);
	}

	@Override
	public void add(final String declaration, final Monster m) {
		try {
			m.fasthealing = Integer.parseInt(declaration.substring(
					declaration.lastIndexOf(' ') + 1, declaration.length()));
		} catch (final RuntimeException e) {
			throw new RuntimeException("Wrong number format for: " + m, e);
		}
	}

	@Override
	public boolean has(Monster monster) {
		return monster.fasthealing > 0;
	}

	@Override
	public float rate(Monster monster) {
		return monster.fasthealing * .075f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.water.add(new FastHealingUpgrade());
	}
}