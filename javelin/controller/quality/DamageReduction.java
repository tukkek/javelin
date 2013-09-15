package javelin.controller.quality;

import javelin.model.unit.Monster;

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
}
