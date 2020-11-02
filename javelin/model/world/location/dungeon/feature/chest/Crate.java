package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.controller.TieredList;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.focus.Staff;
import javelin.model.item.focus.Wand;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Same as {@link Chest} but meant to only give 10% of an average
 * {@link Dungeon#level} reward. This is meant to keep a more steady flow of
 * useful items to players in {@link Dungeon}s, without compromising game
 * balance or just providing fodder.
 *
 * TODO add more avatars - just because it's called crate doesn't mean it can be
 * shown as bags, trapdoors, cabinets, etc.
 *
 * TODO {@link Chest}s should be part of this if we can get all items into
 * proper specialized containers
 *
 * @see RewardCalculator#getgold(float)
 * @author alex
 */
public class Crate extends Chest{
	/**
	 * Helper to determine a tier-appropirate, {@link Dungeon}-specific image for
	 * {@link Crate}s.
	 *
	 * @see DungeonImages
	 * @author alex
	 */
	public static class TieredCrates extends TieredList<String>{
		/** Constructor. */
		public TieredCrates(DungeonTier t){
			super(t);
			addtiered("basket"+RPG.r(1,8),DungeonTier.CAVE);
			addtiered("pot"+RPG.r(1,28),DungeonTier.CAVE);
			addtiered("box"+RPG.r(1,9),DungeonTier.DUNGEON);
			addtiered("sack"+RPG.r(1,31),DungeonTier.DUNGEON);
			addtiered("barrel"+RPG.r(1,9),DungeonTier.KEEP);
			addtiered("crate"+RPG.r(1,7),DungeonTier.KEEP);
			addtiered("vase"+RPG.r(1,23),DungeonTier.TEMPLE);
		}
	}

	static final Collection<Item> ITEMS=new ArrayList<>();
	static final List<Class<? extends Item>> PROHIBITED=List.of(Scroll.class,
			Staff.class,Wand.class);

	static{
		Item.ITEMS.stream()
				.filter(i->i.consumable&&!PROHIBITED.contains(i.getClass()))
				.forEach(i->ITEMS.add(i));
	}

	/** TODO to be used when hidden chests are implemented. */
	int searchdc=10+Dungeon.active.level;

	/** Constructor. */
	public Crate(Integer pool){
		super(pool);
	}

	@Override
	protected Collection<Item> getitems(){
		return ITEMS;
	}

	@Override
	public Image getimage(){
		var c=Dungeon.active.images.get(DungeonImages.CRATE);
		return Images.get(List.of("dungeon","chest","crate",c));
	}
}
