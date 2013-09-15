package javelin.controller.quality;

import javelin.model.unit.Monster;

public class SpellImmunity extends Quality {

	public SpellImmunity() {
		super("spell immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.sr = Integer.MAX_VALUE;
	}

}
