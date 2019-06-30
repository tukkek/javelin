package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Wilderness;
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

	/** Constructor. */
	public RandomDungeonEncounter(Dungeon d){
		this();
		map=Terrain.UNDERGROUND.getmaps().pick();
		weather=Math.max(0,Weather.current-1);
		encounters=d.encounters;
	}

	/** Constructor. */
	public RandomDungeonEncounter(Wilderness w){
		this();
		map=RPG.pick(w.type.getmaps());
		encounters=w.encounters;
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
