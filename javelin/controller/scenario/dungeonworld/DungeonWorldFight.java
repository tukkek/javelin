package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

public class DungeonWorldFight extends Fight {
	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

	@Override
	public Integer getel(int teamel) {
		return ChallengeCalculator.calculateel(Squad.active.members) - 1;
	}
}