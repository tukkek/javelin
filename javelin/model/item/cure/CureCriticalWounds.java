package javelin.model.item.cure;

import tyrant.mikera.engine.RPG;

public class CureCriticalWounds extends CurePotion {

	public CureCriticalWounds() {
		super("critical", 1400, "Cures 4d8+7 hit points");
	}

	@Override
	int rollhpcured() {
		int sum = 7;
		for (int i = 0; i < 4; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
