/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai;

import java.util.List;

/**
 * A minimax tree node.
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
	/**
	 * Valor utilit�rio desse n�.
	 * 
	 * TODO change to 'utility'?
	 * 
	 * @author Alex Henry
	 */
	final public float value;
	public final List<ChanceNode> cns;

	/**
	 * TODO if
	 * {@link ActionProvider#checkstacking(javelin.model.state.BattleState)}
	 * errors don't stop try {@link Node#deepclone()} here instead.
	 */
	public Entry(final Node state, final float value2,
			final List<ChanceNode> cns) {
		super();
		node = state;
		value = value2;
		this.cns = cns;
	}

	@Override
	public String toString() {
		return value + "\n" + node.toString();
	}
}