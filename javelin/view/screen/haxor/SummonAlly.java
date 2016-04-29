package javelin.view.screen.haxor;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.view.screen.SquadScreen;

/**
 * One-time offer to recruit one extra starting squad member from the starting
 * character set.
 * 
 * This is an exception to the rule of not having balance-related features on
 * Haxor's temple but some players may find the start of the game too slow -
 * this would enable them to use their starting ticket to speed up the early
 * game.
 * 
 * @see SquadScreen
 */
public class SummonAlly extends Hax {
	public SummonAlly(String name, double price, boolean requirestargetp) {
		super(name, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		ArrayList<Monster> candidates = SquadScreen.getcandidates();
		Collections.shuffle(candidates);
		float before = Squad.active.members.size();
		candidateloop: for (Monster candidate : candidates) {
			for (Combatant c : Squad.active.members) {
				if (candidate.name.equals(c.source.name)) {
					continue candidateloop;
				}
			}
			Javelin.recruit(candidate);
			break;
		}
		if (before == Squad.active.members.size()) {
			return false;
		}
		return true;
	}
}