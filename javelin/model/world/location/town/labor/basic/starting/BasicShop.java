package javelin.model.world.location.town.labor.basic.starting;

import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.world.location.town.labor.basic.Shop;

/**
 * Same as shop but with a fixed selection of {@link Tier#LOW} items ti prevent
 * players from restarting new games until they end up with a perceived
 * "optimal" choice of items (thus encouraging them to bore theselves, which
 * goes against the DCSS philosophy document). This can still be upgraded as a
 * normal shop later on and as such the initial selection must be keep fairly
 * small.
 *
 * @see BasicAcademy
 *
 * @author alex
 */
public class BasicShop extends Shop{
	/** Constructor. */
	public BasicShop(){
		super();
	}

	@Override
	protected void stock(){
		if(!selection.isEmpty()){
			super.stock();
			return;
		}
		var cheap=Item.randomize(Item.NONPRECIOUS.stream().filter(i->i.price<100)
				.collect(Collectors.toList()));
		selection.addAll(cheap.subList(0,Math.min(5,cheap.size())));
	}

	@Override
	public String getimagename(){
		return "shop";
	}
}
