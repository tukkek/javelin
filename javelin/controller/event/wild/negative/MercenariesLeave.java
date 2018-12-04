package javelin.controller.event.wild.negative;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.event.wild.WildEvent;
import javelin.model.item.Item;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;

/**
 * One or more mercenaries abandon the party.
 *
 * @author alex
 */
public class MercenariesLeave extends WildEvent{
	Combatants leaving=new Combatants(0);

	/** Reflection-friendly constructor. */
	public MercenariesLeave(){
		super("Mercenaries leave");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		var mercenaries=s.getmercenaries();
		if(mercenaries.isEmpty()) return false;
		var stay=s.getbest(Skill.DIPLOMACY).roll(Skill.DIPLOMACY);
		for(var mercenary:mercenaries){
			var leave=mercenary.roll(Skill.DIPLOMACY);
			if(leave>stay) leaving.add(mercenary);
		}
		return !leaving.isEmpty();
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		Javelin.message("After an argument, some mercenaries abandon the ranks:\n"
				+Javelin.group(leaving)+"...",true);
		var items=new ArrayList<Item>(0);
		for(var mercenary:leaving){
			items.addAll(s.equipment.get(mercenary));
			s.remove(mercenary);
		}
		for(var item:items)
			item.grab(s);
	}
}
