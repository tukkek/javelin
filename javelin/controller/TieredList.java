package javelin.controller;

import java.util.ArrayList;

import javelin.model.world.location.dungeon.DungeonTier;

/**
 * A list that filters by a minimum tier and gives a +1 weight for each higher
 * tier compared to elements of lower tier. This is useful when you want a
 * chance of higher tiers selecting certain elements but more likelihood of
 * selecting higher-tier elemenets - mostly for cosmetic purposes.
 *
 * Tiers start at zero but startign at one would not affect the overall utility,
 * althought it would lose some "precision" (in such that the "+1 chance per
 * tier" is alreadu entirely arbitrary to begin with).
 *
 * @author alex
 */
public class TieredList<K>extends ArrayList<K>{
	int tier;

	/** Constructor. */
	public TieredList(int tier){
		this.tier=tier;
	}

	/** Constructor with {@link DungeonTier}. */
	public TieredList(DungeonTier t){
		this(t.tier.getordinal());
	}

	/**
	 * @param tier If lower than the given collection tier, will not add anything.
	 *          Otherwise, add the element once for each tier (beginnign at 0)
	 */
	public void addtiered(K item,int tier){
		if(tier<this.tier) return;
		for(var i=0;i<=tier;i++)
			add(item);
	}

	/** {@link #addtiered(Object, int)} with {@link DungeonTier}. */
	public void addtiered(K type,DungeonTier t){
		addtiered(type,t.tier.getordinal());
	}
}