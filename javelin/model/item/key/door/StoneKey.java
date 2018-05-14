package javelin.model.item.key.door;

import javelin.controller.challenge.RewardCalculator;

public class StoneKey extends Key {
	public StoneKey() {
		super("Stone key", RewardCalculator.getgold(2));
	}
}