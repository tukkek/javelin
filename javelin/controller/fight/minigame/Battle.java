package javelin.controller.fight.minigame;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.fight.Fight;
import javelin.controller.map.terrain.plain.Field;
import javelin.model.unit.Combatant;

/**
 * Not to be confused with the generic battle controller {@link Fight}.
 *
 * @see Battlefield
 * @author alex
 */
public class Battle extends Minigame{
	ArrayList<Combatant> blueteam;
	ArrayList<Combatant> monsters;

	/**
	 * @param blueteam Allied team.
	 * @param monsters Opponents.
	 */
	public Battle(ArrayList<Combatant> blueteam,ArrayList<Combatant> monsters){
		this.blueteam=blueteam;
		this.monsters=monsters;
		map=new Field();
	}

	@Override
	public boolean onend(){
		if(!victory){
			Javelin.message("You lost...",true);
			return false;
		}
		CountingSet counter=new CountingSet();
		counter.casesensitive=true;
		for(Combatant c:state.blueTeam)
			if(!c.summoned) counter.add(c.source.toString());
		Javelin.message("You won!",true);
		return false;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return monsters;
	}

	@Override
	public ArrayList<Combatant> getblueteam(){
		return blueteam;
	}
}
