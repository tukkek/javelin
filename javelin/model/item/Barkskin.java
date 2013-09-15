package javelin.model.item;

import javelin.model.unit.Combatant;

public class Barkskin extends Potion {

	public Barkskin() {
		super("Potion of barkskin +3", 600);
	}

	@Override
	public boolean use(Combatant c) {
		c.source.ac += 3;
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
