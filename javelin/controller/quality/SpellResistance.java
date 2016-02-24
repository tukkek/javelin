package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class SpellResistance extends Quality {

	public SpellResistance() {
		super("sr ");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (m.sr != Integer.MAX_VALUE) {
			m.sr = Integer.parseInt(
					declaration.substring(declaration.indexOf(' ') + 1));
			if (m.sr <= 10) {
				/* CR is given only for SR11+ */
				m.sr = 11;
			}
		}
	}

	@Override
	public boolean has(Monster monster) {
		return 11 <= monster.sr && monster.sr < Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster) {
		return (monster.sr - 10) * .1f;
	}

}
