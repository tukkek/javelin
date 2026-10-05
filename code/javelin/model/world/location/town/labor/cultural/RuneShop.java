package javelin.model.world.location.town.labor.cultural;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.item.gear.rune.Rune;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Shop;

/**
 * A {@link Trait#MAGICAL} shop that only sells {@link Rune}s.
 *
 * @author alex
 */
public class RuneShop extends Shop{
	/** {@link Labor}. */
	public static class BuildRuneShop extends BuildShop{
		/** Constructor. */
		public BuildRuneShop(){
			minimumrank=Rank.VILLAGE;
			cost=10;
		}

		@Override
		public Shop getgoal(){
			return new RuneShop();
		}
	}

	/** Constructor. */
	public RuneShop(){
		super(Rank.VILLAGE.rank);
		description="Rune shop";
	}

	@Override
	protected List<Item> filter(List<Item> items){
		return items.stream().filter(i->i instanceof Rune)
				.collect(Collectors.toList());
	}
}
