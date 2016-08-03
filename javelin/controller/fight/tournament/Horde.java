package javelin.controller.fight.tournament;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.ExhibitionFight;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Exhibition against many opponents.
 * 
 * @author alex
 */
public class Horde extends Exhibition {
	/** Constructor. */
	public Horde() {
		super("Horde");
	}

	@Override
	public void start() {
		throw new StartBattle(new ExhibitionFight() {
			@Override
			public ArrayList<Combatant> getmonsters(int teamel) {
				for (Monster m : new CrIterator(
						Javelin.MONSTERSBYCR.descendingMap())) {
					ArrayList<Combatant> opponents = new ArrayList<Combatant>();
					for (int i = 0; i < EncounterGenerator
							.getmaxenemynumber(); i++) {
						opponents.add(new Combatant(m.clone(), true));
					}
					try {
						if (ChallengeRatingCalculator
								.calculateelsafe(opponents) > teamel) {
							continue;
						}
					} catch (UnbalancedTeams e) {
						continue;
					}
					return opponents;
				}
				throw new RuntimeException(
						"Couldn't generate Horde exhibition");
			}
		});
	}

}
