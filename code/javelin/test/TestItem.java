package javelin.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Debug;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.challenge.Tier;
import javelin.model.item.Item;

/**
 * Helper methods to help test {@link Item}s.
 *
 * @see Debug
 * @author alex
 */
public class TestItem{
	/**
	 * @param items Given these items, checks their {@link Item#price} to see on
	 *          how many {@link Tier}s they belong in. Generally speaking, good
	 *          items types will be accessible in as many tiers as possible.
	 * @param If <code>true</code> will print information to {@link System#out}.
	 * @return The median number of items per tier. This can be used to compare
	 *         different approaches and parameters to systems - a higher number is
	 *         preferrable, generally speaking.
	 */
	public static int testrange(List<? extends Item> items,boolean print){
		var hits=new Integer[]{0,0,0,0};
		for(var i:items)
			for(int t=0;t<=4;t++){
				if(t==4){
					hits[3]+=1;
					break;
				}
				var tier=Tier.TIERS.get(t);
				if(i.price<RewardCalculator.calculatenpcequipment(tier.maxlevel)){
					hits[t]+=1;
					break;
				}
			}
		var result=new ArrayList<>(Arrays.asList(hits));
		result.sort(null);
		int median=(result.get(1)+result.get(2))/2;
		if(print){
			System.out.println(Arrays.asList(hits));
			System.out.println("Median: "+median);
		}
		return median;
	}
}
