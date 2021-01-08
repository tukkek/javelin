package javelin.controller.content.fight.tournament;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.fight.ExhibitionFight;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Exhibition against a shingle opponent.
 *
 * @author alex
 */
public class Champion extends Exhibition{
	/** Constructor. */
	public Champion(){
		super("Champion");
	}

	@Override
	public void start(){
		throw new StartBattle(new ExhibitionFight(){
			@Override
			public ArrayList<Combatant> getfoes(Integer teamel){
				for(Monster m:new CrIterator(Monster.BYCR))
					if(ChallengeCalculator.crtoel(m.cr)>=teamel){
						ArrayList<Combatant> opponents=new ArrayList<>();
						opponents.add(new Combatant(m,true));
						return opponents;
					}
				throw new RuntimeException("couldn't generate Champion exhibition");
			}
		});
	}

}
