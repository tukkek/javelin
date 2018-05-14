package javelin.model.item.key.door;

import javelin.controller.challenge.RewardCalculator;

public class MasterKey extends Key {
	public MasterKey() {
		super("Master key", RewardCalculator.getgold(4));
	}
}
