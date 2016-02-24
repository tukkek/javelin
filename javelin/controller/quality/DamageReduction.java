package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class DamageReduction extends Quality {

	public DamageReduction() {
		super("damage reduction");
	}

	@Override
	public void add(String declaration, Monster m) {
		final int magicbonus = declaration.indexOf('/');
		if (magicbonus >= 0) {
			declaration = declaration.substring(0, magicbonus);
		}
		m.dr = Integer.parseInt(declaration.substring(
				declaration.lastIndexOf(' ') + 1, declaration.length()));
	}

	@Override
	public boolean has(Monster monster) {
		return monster.dr >= 0;
	}

	@Override
	public float rate(Monster monster) {
		return monster.dr * .2f;
	}
}
