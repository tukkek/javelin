package javelin.controller.content.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.Difficulty;
import javelin.controller.content.fight.mutator.Meld;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/**
 * Generates a {@link RandomEncounter} based on {@link Dungeon}
 * {@link Encounter}s. By default always uses a {@link Terrain#UNDERGROUND} map.
 *
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter{
	Combatants encounter;

	/** {@link Dungeon} constructor. */
	public RandomDungeonEncounter(DungeonFloor f){
		set(Terrain.UNDERGROUND);
		mutators.add(new Meld());
		encounter=RPG.pick(f.encounters);
	}

	/** Picks a random map to use from this pool. */
	public void set(Terrain t){
		map=t.getmaps().pick();
		weather=t.getweather();
	}

	@Override
	public ArrayList<Combatant> generate(){
		/*TODO once there is a better strategical skip for encounters, this won't be
		 * necessary anymore.*/
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
