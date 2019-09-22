package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.List;

import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Offers increasingly difficult fights, starting at Level 1 and being removed
 * at {@link #MAXLEVEL}.
 *
 * TODO should probably offer something cooler at the end of it? Maybe make it
 * one of the ways to win the game?
 *
 * @author alex
 */
public class DeepDungeon extends UniqueLocation{
	static final String DESCRIPTION="Deep dungeon";

	List<Dungeon> floors=new ArrayList<>(20);

	/** Constructor. */
	public DeepDungeon(){
		super(DESCRIPTION,DESCRIPTION,20,20);
		generategarrison=false;
		for(var level=Tier.LOW.minlevel;level<=Tier.EPIC.maxlevel;level++){
			var parent=floors.isEmpty()?null:floors.get(floors.size()-1);
			floors.add(new Dungeon(DESCRIPTION,level,parent,floors));
		}
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public boolean interact(){
		return floors.get(0).interact();
	}

	@Override
	public void place(){
		super.place();
		floors.get(0).setlocation(getlocation());
	}
}
