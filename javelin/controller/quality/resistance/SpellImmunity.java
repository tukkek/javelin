package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * See more info on the d20 SRD.
 */
public class SpellImmunity extends Quality {
	/**
	 * See the d20 SRD for more info.
	 */
	static class SpellImmunityUpgrade extends Upgrade {

		public SpellImmunityUpgrade() {
			super("Spell immunity");
		}

		@Override
		public String inform(Combatant m) {
			return "";
		}

		@Override
		public boolean apply(Combatant m) {
			if (m.source.sr == Integer.MAX_VALUE) {
				return false;
			}
			m.source.sr = Integer.MAX_VALUE;
			return true;
		}

	}

	public SpellImmunity() {
		super("spell immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.sr = Integer.MAX_VALUE;
	}

	@Override
	public boolean has(Monster m) {
		return m.sr == Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster m) {
		return 5;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new SpellImmunityUpgrade());
	}

	@Override
	public boolean apply(String attack, Monster m) {
		return super.apply(attack, m) || attack.equals("magic immunity");
	}
}
