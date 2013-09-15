package javelin.controller.ai;

import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class BattleAi extends AbstractAlphaBetaSearch<Node> {
	public BattleAi(final int aiDepth) {
		super(aiDepth);
	}

	@Override
	protected Node catchMemoryIssue(final Error e) {
		throw e;
	}

	// private class UtilityFactor {
	// final public float value;
	// final public float priority;
	//
	// public UtilityFactor(final float value, final float priority) {
	// super();
	// this.value = value;
	// this.priority = priority;
	// }
	// }

	@Override
	public float utility(final Node node) {
		final BattleState state = (BattleState) node;
		final List<Combatant> redTeam = state.getRedTeam();
		if (redTeam.isEmpty()) {
			return Integer.MIN_VALUE;
		}
		final List<Combatant> blueTeam = state.getBlueTeam();
		if (blueTeam.isEmpty()) {
			return Integer.MAX_VALUE;
		}
		return ChallengeRatingCalculator
				.calculatepositive(ChallengeRatingCalculator
						.convertlist(redTeam))
				/ ChallengeRatingCalculator
						.calculatepositive(ChallengeRatingCalculator
								.convertlist(blueTeam))
				/* ***************************************** */
				+ rateunits(redTeam) / rateunits(blueTeam) / 10f
				/* ***************************************** */
				- calculatecloseness(state) / 1000f;
	}

	/**
	 * this shouldn't be done because it causes assymetry in the calculation but
	 * it is needed to make the AI player approach. It introduces the false
	 * logic that the AI always wants to get close and the player always wants
	 * to get away.
	 */
	static private int calculatecloseness(final BattleState state) {
		int sum = 0;
		for (final Combatant c1 : state.redTeam) {
			double minimum = Integer.MAX_VALUE;
			for (final Combatant c2 : state.blueTeam) {
				final double distance = Walker.distance(c1, c2);
				if (distance < minimum) {
					minimum = distance;
				}
			}
			sum += minimum;
		}
		return sum;
	}

	private float rateunits(final List<Combatant> redTeam) {
		float hp = 0;
		for (final Combatant m : redTeam) {
			hp += m.source.challengeRating * m.hp / m.maxhp;
		}
		return hp;
	}

	@Override
	public boolean terminalTest(final Node node) {
		final BattleState state = (BattleState) node;
		return state.getRedTeam().isEmpty() || state.getBlueTeam().isEmpty();
	}
}