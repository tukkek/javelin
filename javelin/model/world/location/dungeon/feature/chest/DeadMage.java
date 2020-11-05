package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import javelin.model.item.Item;
import javelin.model.item.focus.Rod;
import javelin.model.item.focus.Staff;
import javelin.model.item.focus.Wand;
import javelin.model.unit.skill.Spellcraft;
import javelin.view.Images;

/**
 * Specialized chest for items that require {@link Spellcraft} or similar.
 *
 * TODO change for more "thematic" container type
 *
 * @author alex
 */
public class DeadMage extends Chest{
	static final Set<Class<? extends Item>> ALLOWED=Set.of(Wand.class,Staff.class,
			Rod.class);

	/** Constructor. */
	public DeadMage(Integer gold){
		super(gold);
	}

	@Override
	protected boolean allow(Item i){
		return ALLOWED.contains(i.getClass());
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","deadmage"));
	}
}