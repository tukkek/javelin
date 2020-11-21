package javelin.controller.fight;

import javelin.Debug;
import javelin.JavelinApp;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.old.RPG;

/**
 * Fight that happens on the overworld map.
 *
 * @author alex
 */
public class RandomEncounter extends Fight{
	@Override
	public Integer getel(int teamel){
		return Terrain.current().getel(teamel);
	}

	/**
	 * @param chance % chance of starting a battle.
	 * @throws StartBattle
	 */
	static public void encounter(double chance){
		if(!Debug.disablecombat&&RPG.random()<chance){
			Fight f=JavelinApp.context.encounter();
			if(f!=null) throw new StartBattle(f);
		}
	}
}