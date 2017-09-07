package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * This is not an official d20 SRD quality but represents the mind-effect
 * resistance many {@link Monster}s have.
 * 
 * @see Monster#will()
 */
public class MindImmunity extends Quality {
	static class MindImmunityUpgrade extends Upgrade {
		MindImmunityUpgrade() {
			super("Mind immunity");
		}

		@Override
		public String inform(Combatant c) {
			return "Current will: " + c.source.will();
		}

		@Override
		protected boolean apply(Combatant c) {
			if (c.source.immunitytomind) {
				return false;
			}
			c.source.immunitytomind = true;
			return true;
		}

	}

	/** Constructor. */
	public MindImmunity() {
		super("mind immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunitytomind = true;
	}

	@Override
	public boolean has(Monster monster) {
		return monster.immunitytomind;
	}

	@Override
	public float rate(Monster monster) {
		return 0.5f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new MindImmunityUpgrade());
	}
}
