package javelin.controller.ai;

import java.util.List;

import javelin.controller.ai.valueselector.ValueSelector;
import javelin.model.condition.Defending;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Javelin's implementation of {@link AlphaBetaSearch}.
 * 
 * @author alex
 */
public class BattleAi extends AlphaBetaSearch {
	/**
	 * Using {@link Integer#MAX_VALUE} (over 2 billion) could have been making
	 * the AI think taking extremely unlikely actions would be good to win the
	 * game.
	 * 
	 * Ideally should use something that will never be reached by the
	 * {@link #ratechallenge(List)} but not any higher.
	 */
	private static final float LIMIT = 1000;

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
		final float redTeam = BattleAi.ratechallenge(state.getredTeam());
		if (redTeam == 0f) {
			return -LIMIT;
		}
		final float blueTeam = BattleAi.ratechallenge(state.getblueTeam());
		if (blueTeam == 0f) {
			return LIMIT;
		}
		return (redTeam - measuredistances(state.redTeam, state.blueTeam)
				- state.meld.size() - defending(state))
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
			challenge +=
					c.source.challengerating * (1 + c.hp / (float) c.maxhp);
		}
		return challenge;
	}

	static float measuredistances(List<Combatant> us, List<Combatant> them) {
		int score = 0;
		for (Combatant mate : us) {
			int minimum = Integer.MAX_VALUE;
			for (Combatant foe : them) {
				final int distance =
						Math.max(Math.abs(mate.location[0] - foe.location[0]),
								Math.abs(mate.location[1] - foe.location[1]));
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
		final BattleState state = (BattleState) node;
		return state.getredTeam().isEmpty() || state.getblueTeam().isEmpty();
	}

	@Override
	public ValueSelector getplayer(Node node) {
		BattleState s = (BattleState) node;
		return s.blueTeam.contains(s.next) ? minValueSelector
				: maxValueSelector;
	}
}