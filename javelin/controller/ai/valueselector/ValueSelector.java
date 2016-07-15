/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai.valueselector;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.AlphaBetaSearch;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Entry;
import javelin.controller.ai.Node;
import javelin.controller.ai.cache.AiCache;
import javelin.model.state.BattleState;

/**
 * A step in the minimax tree, using alpha-beta prunning.
 * 
 * Base class for min and max selectors.
 * 
 * @author Alex Henry
 * 
 * @see MaxValueSelector
 * @see MinValueSelector
 * @see AlphaBetaSearch
 */
public abstract class ValueSelector {
	private static final PrintStream LOG;
	private static final boolean DEBUGLOG = false;

	static {
		try {
			LOG = ValueSelector.DEBUGLOG ? new PrintStream("ai.log") : null;
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is the worst possible utility value for this player. Used to seed
	 * {@link Entry#value} to force the dummy entry to be replaced with any
	 * valid game state.
	 */
	public final float failure;

	ValueSelector(float failurep) {
		failure = failurep;
	}

	/**
	 * Heart of the minimax search and alpha-beta pod. It's highly recommended
	 * to read the online literature to get a better grasp of what this does.
	 * 
	 * A log can be written when executing this, just mind it that due to the
	 * nature of the search the log is written in reverse order of what it would
	 * be expected to be normally. The identation represents search depth. Each
	 * line is given with the utility value. Where X is shown a pod has been
	 * done, and the alpha/beta value shown as well. Those marked with * were
	 * chosen as the best option.
	 * 
	 * Reducing the thinking time will usually produce less log output, which is
	 * easier for debugging.
	 * 
	 * @param previous
	 *            Last game state, so we can think of what to do now.
	 * @param ai
	 *            Used to encapsulate AI behavior, like utility computation.
	 *            TODO make static
	 * @param depthP
	 *            Current depth (recursion depth).
	 * @param alpha
	 *            Current alpha for this path.
	 * @param beta
	 *            Current beta for this path.
	 * @return The AI's choice of action, the last state before a pod, or the
	 *         given previous state but with it's utility value defined if we
	 *         reached end of recursion.
	 */
	public Entry getValue(final Entry previous,
			@SuppressWarnings("rawtypes") final AlphaBetaSearch ai,
			final int depthP, final float alpha, final float beta,
			ArrayList<Integer> index) {
		AiThread.checkinterrupted();
		final int depth = depthP + 1;
		if (endOfRecursion(previous, ai, depth)) {
			return new Entry(previous.node, ai.utility(previous.cns),
					previous.cns);
		}
		final String ident = calcIdent(depth);
		Entry chosen = previous;
		float a = alpha;
		float b = beta;
		Iterable<List<ChanceNode>> sucessors =
				AiCache.getcache(previous.node, index);
		int i = -1;
		for (final List<ChanceNode> cns : sucessors) {
			i += 1;
			ArrayList<Integer> newindex = (ArrayList<Integer>) index.clone();
			newindex.add(i);
			// final Float utility = AiCache.getutility(newindex, ai, cns);
			for (final ChanceNode cn : cns) {
				final BattleState state = (BattleState) cn.n;
				final ValueSelector selector = ai.getplayer(state);
				Entry outcomeState = selector.getValue(
						new Entry(state, selector.failure, cns), ai, depth, a,
						b, newindex);
				if (ValueSelector.DEBUGLOG) {
					ValueSelector.LOG
							.append("\n" + ident
									+ (selector == ai.maxValueSelector ? "MAX"
											: "MIN")
									+ (outcomeState.value >= 0 ? "+" : "")
									+ outcomeState.value + "|"
									+ cn.action.replaceAll("\n", ","));
				}
				outcomeState = outcomeState.value == chosen.value ? chosen
						: returnBest(chosen, outcomeState);
				if (outcomeState == chosen) {
					continue;
				}
				if (ValueSelector.DEBUGLOG) {
					ValueSelector.LOG.append("*");
				}
				chosen = new Entry(state, outcomeState.value, cns);
				if (testPod(chosen.value, a, b)) {
					if (ValueSelector.DEBUGLOG) {
						ValueSelector.LOG
								.append(" X (a=" + a + " b=" + b + ")");
					}
					return chosen;
				}
				a = different(a, newAlpha(chosen.value, a));
				b = different(b, newBeta(chosen.value, b));
			}
		}
		return chosen;
	}

	private float different(final float b, final float newBeta) {
		if (ValueSelector.DEBUGLOG && b != newBeta) {
			ValueSelector.LOG.append(" Ą");
		}
		return newBeta;
	}

	/**
	 * @param depth
	 *            Current depth.
	 * @return Identing.
	 */
	String calcIdent(final int depth) {
		if (!Javelin.DEBUG) {
			return "";
		}
		String ident = "";

		for (int i = 1; i < depth; i++) {
			ident += " ";
		}

		return ident;
	}

	/**
	 * @param previousState
	 *            State to be check.
	 * @param ai
	 *            TODO make static
	 * @param depth
	 *            Current depth.
	 * 
	 * @return <code>true</code> if {@link AlphaBetaSearch#cutoffTest(int)} is
	 *         necessary or if {@link AlphaBetaSearch#terminalTest(Node)} has
	 *         been reached.
	 * @see
	 */
	private boolean endOfRecursion(final Entry previousState,
			@SuppressWarnings("rawtypes") final AlphaBetaSearch ai,
			final int depth) {
		return ai.cutoffTest(depth) || ai.terminalTest(previousState.node);
	}

	/**
	 * Lets subclasses use the alpha value.
	 * 
	 * @param current
	 *            Last utility.
	 * @param alpha
	 *            Last alpha.
	 * @return new alpha
	 */
	protected float newAlpha(@SuppressWarnings("unused") final float current,
			final float alpha) {
		// delega �s subclasses
		return alpha;
	}

	/**
	 * Lets subclasses use the beta value.
	 * 
	 * @param current
	 *            Last utility.
	 * @param alpha
	 *            Last beta.
	 * @return new beta
	 */
	protected float newBeta(@SuppressWarnings("unused") final float current,
			final float beta) {
		// delega �s subclasses
		return beta;
	}

	/**
	 * Should perform a pod or not.
	 * 
	 * @return <code>false</code> if shot not be prunned.
	 * @author Alex Henry
	 */
	protected abstract boolean testPod(final float utility, final float alpha,
			final float beta);

	/**
	 * @param node
	 *            Given this state, decide what to do next.
	 * @return Player action.
	 */
	abstract protected Entry processCurrent(final Entry node, int depth,
			float alpha, final float beta, ArrayList<Integer> newindex);

	/**
	 * @return The best entry to use of the two given.
	 * @author Alex Henry
	 */
	abstract protected Entry returnBest(final Entry currentBest,
			final Entry processed);
}