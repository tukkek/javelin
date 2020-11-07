package javelin.controller.fight.tournament;

import java.util.ArrayList;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.ExhibitionFight;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * Fight against your own squad.
 *
 * @author alex
 */
public class MirrorMatch extends Match{
	/** Constructor. */
	public MirrorMatch(){
		name="Mirror match";
	}

	@Override
	public void start(){
		throw new StartBattle(new ExhibitionFight(){
			@Override
			public ArrayList<Combatant> getfoes(Integer teamel){
				ArrayList<Combatant> monsters=new ArrayList<>();
				for(Combatant c:Squad.active.members){
					c=c.clone();
					c.clonesource();
					c.source.customName=null;
					c.newid();
					monsters.add(c);
				}
				return monsters;
			}
		});
	}
}
