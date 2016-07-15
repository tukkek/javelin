/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai.valueselector;

import java.util.ArrayList;

import javelin.controller.ai.AlphaBetaSearch;
import javelin.controller.ai.Entry;

/**
 * Represents the human player.
 * 
 * @author Alex Henry
 */
public final class MinValueSelector extends ValueSelector {
	/**
	 * @see #MinValueSelector(AlphaBetaSearch)
	 * 
	 * @author Alex Henry
	 */
	private final AlphaBetaSearch search;

	/**
	 * Constutor.
	 * 
	 * @param search
	 *            Busca atual sendo realizada.
	 * 
	 * @author Alex Henry
	 */
	public MinValueSelector(final AlphaBetaSearch search) {
		super(Float.MAX_VALUE);
		this.search = search;
	}

	@Override
	protected Entry processCurrent(final Entry node, final int depth,
			final float alpha, final float beta,
			final ArrayList<Integer> index) {
		return search.maxValueSelector.getValue(
				new Entry(node.node, -Float.MAX_VALUE, node.cns), search, depth,
				alpha, beta, index);
	}

	@Override
	protected Entry returnBest(final Entry currentBest, final Entry processed) {
		return currentBest.value < processed.value ? currentBest : processed;
	}

	@Override
	protected float newBeta(final float current, final float b) {
		return b > current ? current : b;
	}

	@Override
	protected boolean testPod(final float current, final float a,
			final float b) {
		return current <= a;
	}
}