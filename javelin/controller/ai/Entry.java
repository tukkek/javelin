/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um n� na �rvore de busca minimax.
 * 
 * @author Alex Henry
 */
public class Entry {
	/**
	 * Estado de jogo presente nesse n�.
	 * 
	 * @author Alex Henry
	 */
	final public Node node;
	// TODO change 'value' to 'utility'?
	/**
	 * Valor utilit�rio desse n�.
	 * 
	 * @author Alex Henry
	 */
	final public float value;
	public final List<ChanceNode> cns;

	// /**
	// * Constutor.
	// *
	// * @param node2
	// * Vide {@link #node}.
	// * @param f
	// * Vide {@link #value}.
	// * @author Alex Henry
	// */
	// public Entry(final Node node2, final float f) {
	// this(node2, f, null);
	// }

	public Entry(final Node state, final float value2) {
		node = state;
		value = value2;
		cns = new ArrayList<ChanceNode>();
	}

	public Entry(final Node state, final float value2,
			final List<ChanceNode> cns) {
		super();
		node = state;
		value = value2;
		this.cns = cns;
	}

	public String toString() {
		return value + "\n" + node.toString();
	}
}