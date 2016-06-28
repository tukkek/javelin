package javelin.controller.ai;

import java.util.List;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Javelin's implementation of {@link AbstractAlphaBetaSearch}.
 * 
 * @author alex
 */
public class BattleAi extends AbstractAlphaBetaSearch {
	/**
	 * Using {@link Integer#MAX_VALUE} (over 2 billion) could have been making
	 * the AI think taking extremely unlikely actions would be good to win the
	 * game.
	 * 
	 * Ideally should use something that will never be reached by the
	 * {@link #ratechallenge(List)} but not any higher.
	 */
	private static final float LIMIT = 1000;

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
		final float redTeam = BattleAi.ratechallenge(state.getRedTeam());
		if (redTeam == 0f) {
			return -LIMIT;
		}
		final float blueTeam = BattleAi.ratechallenge(state.getBlueTeam());
		if (blueTeam == 0f) {
			return LIMIT;
		}
		final float base = redTeam - blueTeam;
		final float distances = +measuredistances(state.redTeam, state.blueTeam)
				- measuredistances(state.blueTeam, state.redTeam);
		return base - distances / 1000f;
	}

	static private float ratechallenge(final List<Combatant> team) {
		float challenge = 0f;
		for (final Combatant c : team) {
			challenge += c.source.challengeRating + //
					c.source.challengeRating * (c.hp / (float) c.maxhp) / 100f;
		}
		return challenge;
	}

	/** TODO round to float, really? */
	static private float measuredistances(List<Combatant> us,
			List<Combatant> them) {
		double score = 0;
		for (Combatant me : us) {
			double minimum = Integer.MAX_VALUE;
			for (Combatant he : them) {
				final int distance =
						Math.max(Math.abs(me.location[0] - he.location[0]),
								Math.abs(me.location[1] - he.location[1]));
				minimum = Math.min(minimum, distance);
			}
			score += minimum;
		}
		return Math.round(score);
	}

	@Override
	public boolean terminalTest(final Node node) {
		final BattleState state = (BattleState) node;
		return state.getRedTeam().isEmpty() || state.getBlueTeam().isEmpty();
	}
}