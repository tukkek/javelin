/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai;

import java.util.List;

/**
 * Esta interface representa um estado de jogo poss�vel, possibilitando o alto
 * desacoplamento de {@link AbstractAlphaBetaSearch}.
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
	 * Ao ser invocado, o turno dever� passar para o pr�ximo jogador.
	 * 
	 * @see #getSucessors()
	 * 
	 * @author Alex Henry
	 */
	void changePlayer();
}