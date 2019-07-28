package javelin.model.world.location.town.labor.basic.starting;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;

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
		var cheap=RPG.shuffle(new ArrayList<>(Item.ITEMS.stream()
				.filter(i->i.price<100).collect(Collectors.toList())));
		for(var i=0;i<5&&i<cheap.size();i++)
			selection.add(cheap.get(i));
		//	selection.addAll(List.of(new Potion(new CureLightWounds()),
		//	new Potion(new LesserRestoration()),new Scroll(new Bless())));
		//for(var summon:Summon.select(Summon.SUMMONS,2,1))
		//selection.add(new Eidolon(summon,0));
	}

	@Override
	public String getimagename(){
		return "locationshop";
	}
}
