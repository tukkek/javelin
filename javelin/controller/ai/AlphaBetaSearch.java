package javelin.controller.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.cache.AiCache;
import javelin.controller.ai.valueselector.MaxValueSelector;
import javelin.controller.ai.valueselector.MinValueSelector;
import javelin.controller.ai.valueselector.ValueSelector;

/**
 * Abstract implementation of a minimax decision tree with alpha-beta prunning.
 * Uses {@link ChanceNode}s to represent actions to which the outcome is decided
 * randomly. This is the workhorse of the AI engine and uses
 * {@link ValueSelector}s to represent the players.
 * 
 * This started as a generic AI engine (actually it was taken from a checkers
 * game I did for college, originally) but grew more and more coupled especially
 * as efficiency issues started piling up. It could still probably be used for
 * generic games but probably not without some mild refactoring.
 * 
 * @author Alex Henry
 * 
 * @see #alphaBetaSearch(Node)
 * @see ValueSelector
 * @see AiCache
 * 
 * @param <K>
 *            Implementa��o de {@link Node} que define os poss�veis estados de
 *            jogo.
 */
public abstract class AlphaBetaSearch {
	/**
	 * Construto de IA que age como o jogador artifical.
	 * 
	 * @author Alex Henry
	 */
	public final MaxValueSelector maxValueSelector = new MaxValueSelector(this);
	/**
	 * Construto de IA que prev� as melhores jogadas poss�veis de serem feitas
	 * pelo jogador humano.
	 * 
	 * @author Alex Henry
	 */
	public final MinValueSelector minValueSelector = new MinValueSelector(this);

	/**
	 * Profundidade m�xima permitida para a busca.
	 * 
	 * @author Alex Henry
	 */
	public final int aiDepth;

	public AlphaBetaSearch(final int aiDepth) {
		this.aiDepth = aiDepth;
	}

	/**
	 * Realiza a busca minimax. <br>
	 * <br>
	 * A implementa��o � feita atrav�s de uma busca de profundidade incremental.
	 * Uma vez que o {@link #aiDepth tempo m�ximo} permitido ao computador tenha
	 * se esgotado, a �ltima busca completa � retornada. Caso o tempo tenha sido
	 * esgotado e nenhuma busca tenha sido completada, ele � ignorado e se
	 * retorna a primeira busca completa, quando ela estiver terminada.
	 * 
	 * @param node
	 *            Um {@link Node estado de jogo}, para o qual uma jogada precisa
	 *            ser escolhida.
	 * @return A jogada escolhida.
	 * @author Alex Henry
	 */
	public List<ChanceNode> alphaBetaSearch(final Node node) {
		try {
			ValueSelector selector = getplayer(node);
			List<ChanceNode> result = selector.getValue(
					new Entry(node, selector.failure,
							new ArrayList<ChanceNode>()),
					this, 0, -Float.MAX_VALUE, Float.MAX_VALUE,
					new ArrayList<Integer>(10)).cns;
			if (result.isEmpty()) {// TODO debug
				System.out.println("#emptynodelist");
			}
			return result;
		} catch (final OutOfMemoryError e) {
			catchMemoryIssue(e);
			throw e;
		} catch (final StackOverflowError e) {
			catchMemoryIssue(e);
			throw e;
		}
	}

	/**
	 * @return The {@link ValueSelector} to use for the initial move.
	 */
	abstract public ValueSelector getplayer(Node node);

	/**
	 * Avisa o usu�rio caso haja um problema de mem�ria. <br/>
	 * <br/>
	 * Exemplo:
	 * "A pillha estourou. Voc� precisa executar o programa ajustando os argumentos -Xmx e -Oss da JVM. Vide detalhes na sa�da padr�o."
	 * 
	 * @param e
	 *            O erro a ser avisado.
	 * @return Nada. Ao inv�s disso lan�a uma {@link RuntimeException}.
	 * @author Alex Henry
	 */
	abstract protected Node catchMemoryIssue(final Error e);

	/**
	 * Essa fun��o mede o valor de utilidade de um determinado estado quando
	 * este estado � terminal, e funciona como a fun��o <b>eval</b> para estados
	 * n�o terminais.
	 * 
	 * @param node
	 *            O estado a ser avaliado.
	 * 
	 * @return Uma representa��o num�rica do valor de jogo para este estado.
	 *         Esta pode ser negativa ou positiva, representando uma situa��o
	 *         favor�vel a um dos respectivos jogadores. Os valores mais
	 *         extremos em ambos os casos ser�o aqueles almejados pela IA, e
	 *         geralmente devem ser os valores que indicam fim do jogo.
	 */
	public abstract float utility(final Node node);

	/**
	 * Delega � subclasse a fun��o de verificar se o dado estado de jogo
	 * representa fim de jogo.
	 * 
	 * @param node
	 *            Estado a ser verificado.
	 * @return <code>true</code> caso o estado dado seja realmente fim de jogo
	 *         (vit�ria de algum dos jogadores, ou empate).
	 * @author Alex Henry
	 */
	public abstract boolean terminalTest(final Node node);

	/**
	 * @param depth
	 *            Profundidade atual da busca.
	 * @return <code>true</code> caso a profundidade atual seja igual ou maior
	 *         {@link #aiDepth � m�xima}.
	 * @author Alex Henry
	 */
	public boolean cutoffTest(final int depth) {
		return depth >= aiDepth + 1;
	}

	public float utility(final List<ChanceNode> node) {
		float sum = 0;
		for (final ChanceNode cn : node) {
			sum += cn.chance * utility(cn.n);
		}
		return sum;
	}
}
