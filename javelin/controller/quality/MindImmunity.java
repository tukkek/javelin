package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * This is not an official d20 SRD quality but represents the mind-effect
 * resistance many {@link Monster}s have.
 * 
 * @see Monster#will()
 */
public class MindImmunity extends Quality {

	public MindImmunity() {
		super("mind immunity");
	}

	@Override
	public void add(String declaration, Monster m) {
		m.immunetomindeffects = true;
	}

	@Override
	public boolean has(Monster monster) {
		return monster.immunetomindeffects;
	}

	@Override
	public float rate(Monster monster) {
		return 0.5f;
	}
}
