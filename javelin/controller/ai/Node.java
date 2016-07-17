/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai;

import java.util.List;

import javelin.controller.action.Action;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Describes the game state.
 * 
 * @author Alex Henry
 */
public interface Node extends Cloneable {
	/**
	 * @return Todas as jogadas v�lidas poss�veis de serem feitas pelo jogador
	 *         atual. Note que a t�cnica de poda alfabeta apresenta uma
	 *         tend�ncia a selecionar jogadas apresentadas primeiro, ent�o �
	 *         recomend�vel retornar poss�veis jogadas da maneira mais aleat�ria
	 *         poss�vel, de modo a evitar "maneirismos" por parte do jogador
	 *         artificial.
	 * @author Alex Henry
	 */

	Iterable<List<ChanceNode>> getSucessors();

	@Override
	int hashCode();

	/**
	 * Fast version, used inside {@link Action}.
	 * 
	 * @see BattleState#clone(Combatant)
	 */
	Node clone();

	/**
	 * Slow, complete cloning, used elsewwhere.
	 * 
	 * @see Node#clone()
	 */
	Node clonedeeply();
}