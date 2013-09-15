package javelin.controller.ai;

import tyrant.mikera.tyrant.Game.Delay;

public class ChanceNode {
	public String action;
	public final Node n;
	/**
	 * Ao executar a busca, deve-se usar {@link Node#getSucessors()} e ver qual
	 * o {@link ChanceNode} resultante a ser usado como mapa.
	 */
	public final float chance;
	public Delay delay;

	public ChanceNode(final Node n, final float chance, final String action,
			final Delay delay) {
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
}