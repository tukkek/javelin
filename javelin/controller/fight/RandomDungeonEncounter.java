package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.Difficulty;
import javelin.controller.fight.mutator.Melding;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.terrain.Terrain;
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

	RandomDungeonEncounter(){
		mutators.add(new Melding());
	}

	/** {@link Dungeon} constructor. */
	public RandomDungeonEncounter(DungeonFloor f){
		this();
		encounter=RPG.pick(f.encounters);
		set(Terrain.UNDERGROUND);
	}

	/** Picks a random map to use from this pool. */
	public void set(Terrain t){
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
