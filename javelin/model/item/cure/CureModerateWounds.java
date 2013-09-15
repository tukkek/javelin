package javelin.model.item.cure;

import tyrant.mikera.engine.RPG;

public class CureModerateWounds extends CurePotion {

	public CureModerateWounds() {
		super("moderate", 300, "Cures 2d8+3 hit points");
	}

	@Override
	int rollhpcured() {
		int sum = 3;
		for (int i = 0; i < 2; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
