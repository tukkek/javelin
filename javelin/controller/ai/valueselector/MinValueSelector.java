/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai.valueselector;

import javelin.controller.ai.AbstractAlphaBetaSearch;
import javelin.controller.ai.Entry;

/**
 * Extens�o concreta de {@link AbstractValueSelector} que prev� as melhores
 * jogadas poss�veis pelo jogador humano.
 * 
 * @author Alex Henry
 */
public final class MinValueSelector extends AbstractValueSelector {
	/**
	 * @see #MinValueSelector(AbstractAlphaBetaSearch)
	 * 
	 * @author Alex Henry
	 */
	private final AbstractAlphaBetaSearch<?> search;

	/**
	 * Constutor.
	 * 
	 * @param search
	 *            Busca atual sendo realizada.
	 * 
	 * @author Alex Henry
	 */
	public MinValueSelector(final AbstractAlphaBetaSearch<?> search) {
		super();
		this.search = search;
	}

	@Override
	protected Entry processCurrent(final Entry node, final int depth,
			final float alpha, final float beta) throws InterruptedException {
		return search.maxValueSelector.getValue(new Entry(node.node,
				Integer.MIN_VALUE, node.cns), search, depth, alpha, beta);
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
	protected boolean testPod(final float current, final float a, final float b) {
		return current <= a;
	}
}