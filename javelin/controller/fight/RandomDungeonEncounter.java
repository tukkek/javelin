package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.Difficulty;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 *
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter{
	Combatants encounter;

	RandomDungeonEncounter(){
		meld=true;
	}

	/** {@link Dungeon} constructor. */
	public RandomDungeonEncounter(DungeonFloor f){
		this();
		encounter=RPG.pick(f.encounters);
		setterrain(RPG.pick(f.dungeon.terrains));
	}

	/** Picks a random map to use from this pool. */
	public void setterrain(Terrain t){
		map=t.getmaps().pick();
		weather=t.getweather();
	}

	@Override
	public ArrayList<Combatant> generate(){
		/*TODO once there is a better strategical skip for encounters, this won't be
		 * encessary anymore.*/
		if(Difficulty.calculate(Squad.active.members,
				encounter)<=Difficulty.VERYEASY)
			return null;
		return encounter==null?null:encounter.generate();
	}

	@Override
	public boolean avoid(List<Combatant> foes){
		return foes==null||super.avoid(foes);
	}
}
