package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class SpellImmunity extends Quality {

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

}
