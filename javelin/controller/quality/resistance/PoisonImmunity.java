package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Reader and {@link Upgrade} for poison immunity.
 * 
 * @see Monster#will()
 */
public class PoisonImmunity extends Quality {
	static class PoisonImmunityUpgrade extends Upgrade {
		PoisonImmunityUpgrade() {
			super("Poison immunity");
		}

		@Override
		public String inform(Combatant c) {
			return "";
		}

		@Override
		protected boolean apply(Combatant c) {
			if (c.source.immunitytopoison) {
				return false;
			}
			c.source.immunitytopoison = true;
			return true;
		}

	}

	/** Constructor. */
	public PoisonImmunity() {
		super("poison immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunitytopoison = true;
	}

	@Override
	public boolean has(Monster monster) {
		return monster.immunitytopoison;
	}

	@Override
	public float rate(Monster monster) {
		return 0.2f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new PoisonImmunityUpgrade());
	}
}
