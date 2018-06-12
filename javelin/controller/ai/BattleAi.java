package javelin.controller.ai;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.valueselector.ValueSelector;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Defending;

/**
 * Javelin's implementation of {@link AlphaBetaSearch}.
 *
 * @author alex
 */
public class BattleAi extends AlphaBetaSearch {
	/**
	 * Ideally should use something that will never be reached by the
	 * {@link #ratechallenge(List)} but not any higher.
	 */
	private static final float LIMIT = Float.MAX_VALUE;

	/** Constructor. */
	public BattleAi(final int aiDepth) {
		super(aiDepth);
	}

	@Override
	protected Node catchMemoryIssue(final Error e) {
		throw e;
	}

	@Override
	public float utility(final Node node) {
		final BattleState state = (BattleState) node;
		final float redTeam = BattleAi.ratechallenge(state.getredteam());
		if (redTeam == 0f) {
			return LIMIT;
		}
		final float blueTeam = BattleAi.ratechallenge(state.getblueteam());
		if (blueTeam == 0f) {
			return -LIMIT;
		}
		return (redTeam - measuredistances(state.redTeam, state.blueTeam))
				- state.meld.size() - defending(state)
				- (blueTeam - measuredistances(state.blueTeam, state.redTeam));
	}

	static float defending(BattleState state) {
		int ndefending = 0;
		for (Combatant c : state.redTeam) {
			if (c.hascondition(Defending.class) != null) {
				ndefending += 1;
			}
		}
		return ndefending;
	}

	static private float ratechallenge(final List<Combatant> team) {
		float challenge = 0f;
		for (final Combatant c : team) {
			challenge += c.source.cr
					* (1 + c.hp / (float) c.maxhp);
		}
		return challenge;
	}

	static float measuredistances(List<Combatant> us, List<Combatant> them) {
		int score = 0;
		for (Combatant mate : us) {
			int minimum = Integer.MAX_VALUE;
			for (Combatant foe : them) {
				final int distance = Walker.distanceinsteps(mate.location[0],
						mate.location[1], foe.location[0], foe.location[1]);
				if (distance < minimum) {
					minimum = distance;
				}
			}
			score += minimum;
		}
		return score / 125f;
	}

	@Override
	public boolean terminalTest(final Node node) {
		if (Javelin.app.fight.endless) {
			return false;
		}
		final BattleState state = (BattleState) node;
		return state.redTeam.isEmpty() || state.blueTeam.isEmpty();
	}

	@Override
	public ValueSelector getplayer(Node node) {
		BattleState s = (BattleState) node;
		return s.blueTeam.contains(s.next) ? minValueSelector
				: maxValueSelector;
	}
}