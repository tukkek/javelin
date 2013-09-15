package javelin.model.item;

import javelin.model.unit.Combatant;

public class ResistEnergy extends Potion {
	public ResistEnergy() {
		super("Potion of resist energy 6", 1100);
	}

	@Override
	public boolean use(Combatant c) {
		c.source.resistance += 6;
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}
}
