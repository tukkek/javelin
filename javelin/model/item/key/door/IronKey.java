package javelin.model.item.key.door;

import javelin.controller.challenge.RewardCalculator;

public class IronKey extends Key{
	public IronKey(){
		super("Iron key",RewardCalculator.getgold(3));
	}

}