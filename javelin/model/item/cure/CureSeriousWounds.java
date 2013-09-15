package javelin.model.item.cure;

import tyrant.mikera.engine.RPG;

public class CureSeriousWounds extends CurePotion {

	public CureSeriousWounds() {
		super("serious", 750, "Cures 3d8+5 hit points");
	}

	@Override
	int rollhpcured() {
		int sum = 5;
		for (int i = 0; i < 3; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
