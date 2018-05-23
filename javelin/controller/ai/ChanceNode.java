package javelin.controller.ai;

import javelin.old.Game.Delay;
import javelin.view.mappanel.Overlay;

/**
 * Some action results are determined by chance, like if an attack succeeds at
 * hitting the target or not. This represents one of the possible resuts.
 * 
 * @author alex
 */
public class ChanceNode implements Cloneable {
	/**
	 * Description of outcome.
	 */
	public String action;
	/**
	 * Actual outcome.
	 */
	public Node n;
	/**
	 * Chance of this outcome happening.
	 */
	public final float chance;
	/**
	 * Delay for the outcome.
	 */
	public Delay delay;

	/** Allows a computer action to provide visual feedback. */
	public Overlay overlay = null;

	public ChanceNode(final Node n, final float chance, final String action, final Delay delay) {
		super();
		this.n = n;
		this.chance = chance;
		this.action = action;
		this.delay = delay;
	}

	@Override
	public String toString() {
		return chance * 100 + "% " + action + "\n";
	}

	@Override
	public ChanceNode clone() {
		try {
			ChanceNode clone = (ChanceNode) super.clone();
			clone.n = n.clonedeeply();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}