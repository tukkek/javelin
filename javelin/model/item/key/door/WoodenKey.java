package javelin.model.item.key.door;

import javelin.controller.challenge.RewardCalculator;

public class WoodenKey extends Key {
	public WoodenKey() {
		super("Wooden key", RewardCalculator.getgold(1));
	}
}