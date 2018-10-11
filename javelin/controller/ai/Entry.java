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
public class Entry{
	/**
	 * Game state.
	 */
	final public Node node;
	/**
	 * Utility value.
	 */
	final public float value;
	/**
	 * Outcome probabilities by chance.
	 */
	public final List<ChanceNode> cns;

	/** Constructor. */
	public Entry(final Node state,final float value2,final List<ChanceNode> cns){
		super();
		node=state;
		value=value2;
		this.cns=cns;
		// if (Javelin.DEBUG && cns.isEmpty()) {
		// System.out.println("empty");
		// }
	}

	@Override
	public String toString(){
		return value+"\n"+cns+"\n"+node.toString();
	}
}