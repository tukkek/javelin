package javelin.controller.content.wish;

import java.util.ArrayList;

import javelin.model.item.Item;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;

/**
 * Return an unit to it's original {@link Monster} form. Keep all the XP though
 * so it can be spent again.
 *
 * @author alex
 */
public class Rebirth extends Wish{
	/** Constructor. */
	public Rebirth(Character keyp,WishScreen s){
		super("rebirth to base form",keyp,1,true,s);
	}

	@Override
	boolean wish(Combatant target){
		Monster m=Monster.get(target.source.name);
		Combatant reborn=new Combatant(m,true);
		Squad.active.members.remove(target);
		Squad.active.members.add(reborn);
		reborn.source.customName=target.source.name;
		Squad.active.sort();
		float xp=target.source.cr-reborn.source.cr;
		if(xp>0) reborn.learn(xp);
		ArrayList<Item> equipment=Squad.active.equipment.get(target);
		Squad.active.equipment.remove(target);
		Squad.active.equipment.put(reborn,equipment);
		for(Gear a:new ArrayList<>(target.equipped)){
			target.unequip(a);
			reborn.equip(a);
		}
		return true;
	}
}