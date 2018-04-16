package javelin.controller.fight.tournament;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.ExhibitionFight;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * Exhibition against a shingle opponent.
 * 
 * @author alex
 */
public class Champion extends Exhibition {
	/** Constructor. */
	public Champion() {
		super("Champion");
	}

	@Override
	public void start() {
		throw new StartBattle(new ExhibitionFight() {
			@Override
			public ArrayList<Combatant> getmonsters(Integer teamel) {
				for (Monster m : new CrIterator(Javelin.MONSTERSBYCR)) {
					if (ChallengeCalculator.crtoel(m.cr) >= teamel) {
						ArrayList<Combatant> opponents = new ArrayList<Combatant>();
						opponents.add(new Combatant(m, true));
						return opponents;
					}
				}
				throw new RuntimeException(
						"couldn't generate Champion exhibition");
			}
		});
	}

}
