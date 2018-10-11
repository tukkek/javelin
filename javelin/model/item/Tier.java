package javelin.model.item;

import java.util.HashMap;
import java.util.HashSet;

public enum Tier{
	LOW,MID,HIGH,PARAGON,EPIC,DEMIGOD,GOD,DEITY;

	public static final HashMap<Tier,HashSet<Item>> ITEMS=new HashMap<>(
			Tier.values().length);

	public static Tier[] INTENDED=new Tier[]{LOW,MID,HIGH,PARAGON};

	static{
		for(Tier t:Tier.values())
			ITEMS.put(t,new HashSet<Item>());
	}

	public static Tier get(int level){
		if(level<=5) return LOW;
		if(level<=10) return MID;
		if(level<=15) return HIGH;
		if(level<=20) return PARAGON;
		if(level<=40) return EPIC;
		if(level<=50) return DEMIGOD;
		if(level<=55) return GOD;
		if(level<=60) return DEITY;
		throw new RuntimeException("Tier level is over nine thousaaaand!");
	}
}