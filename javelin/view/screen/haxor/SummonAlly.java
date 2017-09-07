package javelin.view.screen.haxor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
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
	/**
	 * If not <code>null</code> will use this as the challenge rating target.
	 */
	public Float fixed = null;

	/** Constructor. */
	public SummonAlly(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		Float cr = getstartingcr();
		Monster ally = null;
		while (ally == null) {
			List<Monster> candidates = Javelin.MONSTERSBYCR.get(cr);
			if (candidates == null) {
				candidates = Collections.emptyList();
			}
			Collections.shuffle(candidates);
			candidateloop: for (Monster candidate : candidates) {
				for (Combatant c : Squad.active.members) {
					if (candidate.name.equals(c.source.name)) {
						continue candidateloop;
					}
				}
				ally = candidate;
				break;
			}
			Float newcr = findnextlowercr(cr);
			if (newcr == -Float.MAX_VALUE) {
				return false;
			}
			cr = newcr;
		}
		Javelin.recruit(ally);
		return true;
	}

	/**
	 * @return The challenge rating to start selecting new members from. If none
	 *         is found will go down on {@link Javelin#MONSTERSBYCR} from here.
	 */
	protected Float getstartingcr() {
		if (fixed != null) {
			return fixed;
		}
		ArrayList<Combatant> current =
				new ArrayList<Combatant>(Squad.active.members);
		current.addAll(Squad.active.members);
		for (Combatant c : current) {
			/* update challenge rating */
			ChallengeRatingCalculator.calculatecr(c.source);
		}
		current.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.source.challengerating
						.compareTo(o2.source.challengerating);
			}
		});
		return current.get(Math.round(Math.round(
				Math.floor(current.size() / 2.0)))).source.challengerating;
	}

	Float findnextlowercr(Float cr) {
		Float newcr = -Float.MAX_VALUE;
		for (Float c : Javelin.MONSTERSBYCR.keySet()) {
			if (c < cr) {
				newcr = c;
			}
		}
		return newcr;
	}
}