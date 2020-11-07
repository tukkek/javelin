package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.controller.TieredList;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Same as {@link Chest} but meant to only give 10% of an average
 * {@link DungeonFloor#level} reward. This is meant to keep a more steady flow
 * of useful items to players in {@link DungeonFloor}s, without compromising
 * game balance or just providing fodder.
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
	 * Helper to determine a tier-appropirate, {@link DungeonFloor}-specific image
	 * for {@link Crate}s.
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

	static final List<Class<? extends Item>> ALLOWED=List.of(Potion.class,
			Eidolon.class,Scroll.class);

	/** Constructor. */
	public Crate(Integer pool){
		super(pool);
		draw=false;
		searchdc=-5;
		if(searchdc<2) searchdc=2;
	}

	@Override
	public Image getimage(){
		var c=Dungeon.active.dungeon.images.get(DungeonImages.CRATE);
		return Images.get(List.of("dungeon","chest","crate",c));
	}

	@Override
	protected boolean allow(Item i){
		return i.consumable&&ALLOWED.contains(i.getClass());
	}

	@Override
	public boolean discover(Combatant searching,int searchroll){
		var d=super.discover(searching,searchroll);
		if(d) draw=true;
		return d;
	}

	@Override
	public boolean activate(){
		return draw&&super.activate();
	}
}
