package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see Monster#immunitytocritical
 * @author alex
 */
public class CriticalImmunity extends Quality {
	static class CriticalImmunityUpgrade extends Upgrade {
		CriticalImmunityUpgrade() {
			super("Critical immunity");
		}

		@Override
		public String inform(Combatant c) {
			return "Not immune.";
		}

		@Override
		protected boolean apply(Combatant c) {
			if (c.source.immunitytocritical) {
				return false;
			}
			c.source.immunitytocritical = true;
			return true;
		}

	}

	/** Constructor. */
	public CriticalImmunity() {
		super("Critical immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunitytocritical = true;
	}

	@Override
	public boolean has(Monster m) {
		return m.immunitytocritical;
	}

	@Override
	public float rate(Monster m) {
		return .5f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.fire.add(new CriticalImmunityUpgrade());
	}
}
