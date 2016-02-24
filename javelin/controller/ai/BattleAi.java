package javelin.controller.ai;

import java.util.List;

import javelin.controller.walker.Walker;
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
		if (redTeam == 0) {
			return -LIMIT;
		}
		final float blueTeam = BattleAi.ratechallenge(state.getBlueTeam());
		if (blueTeam == 0) {
			return LIMIT;
		}
		return redTeam - blueTeam - state.next.ap / 10
				- BattleAi.summinimumdistances(state) / 10;
	}

	static private float ratechallenge(final List<Combatant> team) {
		float challenge = 0f;
		for (final Combatant c : team) {
			challenge += c.source.challengeRating + //
					c.source.challengeRating * (c.hp / (float) c.maxhp) / 100f;
		}
		return challenge;
	}

	/**
	 * this shouldn't be done because it causes assymetry in the calculation but
	 * it is needed to make the AI player approach. It introduces the false
	 * logic that the AI always wants to get close and the player always wants
	 * to get away.
	 */
	static private float summinimumdistances(final BattleState state) {
		final int bluesize = state.blueTeam.size();
		float sum = 0;
		for (final Combatant c1 : state.redTeam) {
			float minimum = (float) Walker.distance(c1, state.blueTeam.get(0));
			for (int i = 1; i < bluesize; i += 1) {
				final float distance =
						(float) Walker.distance(c1, state.blueTeam.get(i));
				if (distance < minimum) {
					minimum = distance;
				}
			}
			sum += minimum;
		}
		return sum;
	}

	@Override
	public boolean terminalTest(final Node node) {
		final BattleState state = (BattleState) node;
		return state.getRedTeam().isEmpty() || state.getBlueTeam().isEmpty();
	}
}