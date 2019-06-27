package javelin.model.world.location.town.labor.basic.starting;

import java.util.List;

import javelin.model.item.Tier;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Potion;
import javelin.model.item.consumable.Scroll;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.abilities.spell.conjuration.healing.LesserRestoration;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
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
		selection.addAll(List.of(new Potion(new CureLightWounds()),
				new Potion(new LesserRestoration()),new Scroll(new Bless())));
		for(var summon:Summon.select(Summon.SUMMONS,2,1))
			selection.add(new Eidolon(summon));
	}

	@Override
	public String getimagename(){
		return "locationshop";
	}
}
