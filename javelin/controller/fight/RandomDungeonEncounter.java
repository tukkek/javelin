package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
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
	List<Combatants> encounters;

	RandomDungeonEncounter(){
		meld=true;
	}

	/** {@link Dungeon} constructor. */
	public RandomDungeonEncounter(DungeonFloor d){
		this();
		map=d.terrain.getmaps().pick();
		weather=d.terrain.getweather();
		encounters=d.encounters;
	}

	@Override
	public ArrayList<Combatant> generate(){
		Combatants encounter=RPG.pick(encounters);
		/*TODO once there is a better strategical skip for encounters, this won't be
		* encessary anymore.*/
		return encounter!=null&&ChallengeCalculator.calculateel(encounter)
				-Squad.active.getel()>Difficulty.VERYEASY?encounter.generate():null;
	}

	@Override
	public boolean avoid(List<Combatant> foes){
		return foes==null||super.avoid(foes);
	}
}
