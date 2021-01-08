package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.content.upgrade.Upgrade;

public class UpgradeByNameComparator implements Comparator<Upgrade>{
	public static final Comparator<Upgrade> INSTANCE=new UpgradeByNameComparator();

	private UpgradeByNameComparator(){
		// prevent instantiation
	}

	@Override
	public int compare(Upgrade o1,Upgrade o2){
		return o1.name.compareTo(o2.name);
	}
}