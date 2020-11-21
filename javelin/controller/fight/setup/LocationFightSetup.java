package javelin.controller.fight.setup;

import java.util.ArrayList;

import javelin.controller.fight.Fight;
import javelin.controller.map.location.LocationMap;
import javelin.model.unit.Combatant;

public class LocationFightSetup extends BattleSetup{
	final LocationMap map;

	/**
	 * @param hauntFight
	 */
	public LocationFightSetup(LocationMap map){
		this.map=map;
	}

	@Override
	protected void place(boolean strict){
		clear(Fight.state.blueTeam);
		clear(Fight.state.redTeam);
		place(Fight.state.blueTeam,map.spawnblue);
		placeredteam();
	}

	public void placeredteam(){
		place(Fight.state.redTeam,map.spawnred);
	}

	protected void clear(ArrayList<Combatant> team){
		for(Combatant c:team){
			c.location[0]=-1;
			c.location[1]=-1;
		}
	}
}