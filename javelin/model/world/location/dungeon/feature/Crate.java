package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.focus.Staff;
import javelin.model.item.focus.Wand;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;

/**
 * Same as {@link Chest} but meant to only give 10% of an average
 * {@link Dungeon#level} reward. This is meant to keep a more steady flow of
 * useful items to players in {@link Dungeon}s, without compromising game
 * balance or just providing fodder.
 *
 * TODO add more avatars - just because it's called crate doesn't mean it can be
 * shown as bags, trapdoors, cabinets, etc.
 *
 * @see RewardCalculator#getgold(float)
 * @author alex
 */
public class Crate extends Chest{
	static final List<String> AVATARS=List.of("sack","pot","barrel","crate");
	static final Collection<Item> ITEMS=new ArrayList<>();
	static final List<Class<? extends Item>> PROHIBITED=List.of(Scroll.class,
			Staff.class,Wand.class);

	static{
		Item.ITEMS.stream()
				.filter(i->i.consumable&&!PROHIBITED.contains(i.getClass()))
				.forEach(i->ITEMS.add(i));
		//		Item.ITEMS.stream().filter(i->i instanceof PreciousObject)
		//				.forEach(i->ITEMS.add(i));
	}

	/** TODO to be used when hidden chests are implemented. */
	int searchdc=10+Dungeon.active.level;

	/** Constructor. */
	public Crate(int pool){
		super(pool);
		nitems=1;
		var t=DungeonTier.get(Dungeon.active.level).tier.getordinal();
		avatarfile="dungeon"+AVATARS.get(t);
	}

	@Override
	protected Collection<Item> getitems(){
		return ITEMS;
	}
}
