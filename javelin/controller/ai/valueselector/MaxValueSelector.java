/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai.valueselector;

import javelin.controller.ai.AbstractAlphaBetaSearch;
import javelin.controller.ai.Entry;

/**
 * Implementa��o de {@link AbstractValueSelector} que representa o oponente
 * virtual (IA).
 * 
 * @author Alex Henry
 */
public class MaxValueSelector extends AbstractValueSelector {
	/**
	 * @see MaxValueSelector#MaxValueSelector(AbstractAlphaBetaSearch)
	 * 
	 * @author Alex Henry
	 */
	private final AbstractAlphaBetaSearch<?> search;

	/**
	 * Construtor.
	 * 
	 * @param search
	 *            Busca atual sendo realizada.
	 * @author Alex Henry
	 */
	public MaxValueSelector(final AbstractAlphaBetaSearch<?> search) {
		super();
		this.search = search;
	}

	@Override
	protected Entry processCurrent(final Entry node, final int depth,
			final float alpha, final float beta) throws InterruptedException {
		return search.minValueSelector.getValue(new Entry(node.node,
				Integer.MAX_VALUE, node.cns), search, depth, alpha, beta);
	}

	@Override
	protected Entry returnBest(final Entry currentBest, final Entry processed) {
		return currentBest.value > processed.value ? currentBest : processed;
	}

	@Override
	protected float newAlpha(final float current, final float alpha) {
		return alpha < current ? current : alpha;
	}

	@Override
	protected boolean testPod(final float current, final float alpha,
			final float beta) {
		return current >= beta;
	}
}