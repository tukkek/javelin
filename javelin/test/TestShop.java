package javelin.test;

import javelin.model.item.Item;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.gear.RingOfProtection;
import javelin.model.item.potion.Potion;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.world.location.order.CraftingOrder;
import javelin.model.world.location.town.labor.basic.Shop;

/**
 * @see Shop
 * @author alex
 */
public class TestShop{
	/**
	 * According to https://www.d20pfsrd.com/magic-items/magic-item-creation/
	 */
	public static void testeta(){
		Item i=new Potion(new CureLightWounds());
		if(CraftingOrder.calculateeta(i)!=24)
			throw new RuntimeException(i.toString());
		i=new Scroll(new CureLightWounds());
		if(CraftingOrder.calculateeta(i)!=24)
			throw new RuntimeException(i.toString());
		i=new Eidolon(new Summon("azer",1),0);
		if(CraftingOrder.calculateeta(i)!=24)
			throw new RuntimeException(i.toString());
		i=new Eidolon(new Summon("darkmantle",1),0);
		if(CraftingOrder.calculateeta(i)!=24)
			throw new RuntimeException(i.toString());
		i=new RingOfProtection(+5,50000);
		if(CraftingOrder.calculateeta(i)!=4*24/8*(50000/1000))
			throw new RuntimeException(i.toString());
	}
}
