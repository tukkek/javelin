package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * See more info on the d20 SRD.
 */
public class DamageReduction extends Quality {

	/**
	 * See the d20 SRD for more info.
	 */
	static class DamageReductionUpgrade extends Upgrade {

		public DamageReductionUpgrade() {
			super("Damage reduction");
		}

		@Override
		public String inform(Combatant m) {
			return "Currently reducing " + m.source.dr + " points of damage";
		}

		@Override
		public boolean apply(Combatant m) {
			m.source.dr += 5;
			// design parameter
			return m.source.dr <= 5
					+ Math.round(Math.floor(m.source.hd.count() / 2f));
		}

	}

	public DamageReduction() {
		super("damage reduction");
	}

	@Override
	public void add(String declaration, Monster m) {
		boolean singleelement = declaration.contains("silver");
		final int magicbonus = declaration.indexOf('/');
		if (magicbonus >= 0) {
			declaration = declaration.substring(0, magicbonus);
		}
		m.dr = Integer.parseInt(declaration.substring(
				declaration.lastIndexOf(' ') + 1, declaration.length()));
		if (singleelement) {
			/* instead of implementing element convert to full DR */
			m.dr = m.dr / 3;
		}
	}

	@Override
	public boolean has(Monster monster) {
		return monster.dr > 0;
	}

	@Override
	public float rate(Monster monster) {
		return monster.dr * .2f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new DamageReductionUpgrade());
	}

	@Override
	public String describe(Monster m) {
		return "damage reduction " + m.dr;
	}
}
