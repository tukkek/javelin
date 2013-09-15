package javelin.model.item.cure;

import tyrant.mikera.engine.RPG;

public class CureLightWounds extends CurePotion {

	public CureLightWounds() {
		super("light", 50, "Cures 1d8+1 hit points");
	}

	@Override
	int rollhpcured() {
		return RPG.r(1, 8) + 1;
	}

}
