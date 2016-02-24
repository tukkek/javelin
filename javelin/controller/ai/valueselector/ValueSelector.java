/**
 * Alex Henry on 25/09/2009
 */
package javelin.controller.ai.valueselector;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.AbstractAlphaBetaSearch;
import javelin.controller.ai.ActionProvider;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Entry;
import javelin.controller.ai.Node;
import javelin.controller.ai.cache.AiCache;
import javelin.model.state.BattleState;

/**
 * A step in the minimax tree, using alpha-beta prunning.
 * 
 * Sorry about the portuguese comments and encoding issues on them. I'm leaving
 * that in for future translation.
 * 
 * Essa � uma classe abstrata que fatora o c�digo que � comum �s fun��es min e
 * maxValue. O m�todo
 * {@link #getValue(Entry, AbstractAlphaBetaSearch, int, int, int)} cont�m o
 * n�cleo do algoritmo cl�ssico de poda alfabeta. <br>
 * <br>
 * � usado um cache para os valores utilit�rios j� calculados.
 * 
 * @author Alex Henry
 * 
 * @see MaxValueSelector
 * @see MinValueSelector
 * @see AbstractAlphaBetaSearch
 */
public abstract class ValueSelector {
	private static final PrintStream LOG;
	private static final boolean DEBUGLOG = false;

	static {
		try {
			LOG = ValueSelector.DEBUGLOG ? new PrintStream("ai.log") : null;
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Este m�todo � o cora��o da busca minimax e poda alfabeta. � dif�cil
	 * documento-lo, devido a sua complexidade original (por ser um algoritmo
	 * recursivo, para come�ar) e aquela adicionada em prol de um baixo
	 * acoplamento e alta reutiliza��o dessa classe. � sugerido recorrer �
	 * literatura did�tica especializada para maiores detalhes.<br>
	 * <br>
	 * Por�m, o log escrito por essa classe � altamente verboso e detalhado, e
	 * pode e deve ser usado para acompanhar o racioc�nio do oponente
	 * artificial. Ele � gravado no diret�rio de execu��o do JAR do jogo, sob o
	 * nome <code>damIAs.log</code>. O caminho exato do log � escrito na
	 * {@link System#out sa�da padr�o do sistema operacional}: para v�-lo, basta
	 * executar o JAR atrav�s de uma linha de comando (por exemplo, executando o
	 * comando <code>java -jar damIAs.jar</code> no diret�rio aonde o JAR se
	 * encontra). Outra possibilidade � redirecionar essa sa�da para um arquivo
	 * - como fazer isso varia dependendo do SO utilizado.<br>
	 * <br>
	 * No log, cada linha indica uma jogada considerada e seu respectivo valor
	 * utilit�rio, sendo estas identadas de acordo com a profundidade da busca
	 * no momento da escrita da linha. Vale lembrar que os jogadores (min e max)
	 * se revezam a cada vez que a identa��o aumenta. Linhas contendo
	 * <code>X</code> s�o aquelas onde ocorreu poda alfabeta (e os valores de
	 * alfa e beta na ocasi�o s�o exibidos). Linhas contendo asteriscos s�o
	 * aquelas que foram consideradas melhores do que as anteriores de mesma
	 * profundidade pela implementa��o concreta dessa classe � qual a
	 * {@link #returnBest(Entry, Entry) decis�o foi delegada}. Quando alfa ou
	 * beta s�o atualizados, os novos valores s�o impressos nas linhas
	 * correspondentes. Como nota final, vale lembrar que a �rvore contraria um
	 * pouco o senso comum, uma vez que a busca em profundidade resulta na
	 * escrita das folhas antes dos n�s pais - aconselha-se diminuir a
	 * dificuldade do jogo para auxiliar na visualiza��o.
	 * 
	 * @param previous
	 *            Essa � a �ltima jogada feita, ela � usada para verificar todas
	 *            as poss�veis jogadas a partir dela, dessa maneira expandindo a
	 *            profundidade da �rvore de busca.
	 * @param ai
	 *            Implementa��o de busca minimax sendo utilizada.
	 * @param depthP
	 *            Profunidade atual da busca (n�vel de recurs�o).
	 * @param alpha
	 *            Valor alfa para essa caminho na �rvore de busca.
	 * @param beta
	 *            Valor beta para essa caminho na �rvore de busca.
	 * @return A jogada escolhida pela implementa��o concreta dessa classe como
	 *         melhor jogada a realizar, a �ltima jogada antes de uma poda, ou a
	 *         jogada dada no par�metro <code>previousState</code> por�m com o
	 *         valor de utilidade tenha sido calculado (caso tenhamos atingido o
	 *         fim a recurs�o).
	 * @author Alex Henry
	 * @param id
	 */
	public Entry getValue(final Entry previous,
			@SuppressWarnings("rawtypes") final AbstractAlphaBetaSearch ai,
			final int depthP, final float alpha, final float beta,
			ArrayList<Integer> index) {
		AiThread.checkinterrupted();
		final int depth = depthP + 1;
		if (endOfRecursion(previous, ai, depth)) {
			return new Entry(previous.node, ai.utility(previous.cns),
					previous.cns);
		}
		final String ident = calcIdent(depth);
		Entry chosen = previous;
		float a = alpha;
		float b = beta;
		Iterable<List<ChanceNode>> sucessors =
				AiCache.getcache(previous.node, index);
		int i = -1;
		for (final List<ChanceNode> cns : sucessors) {
			i += 1;
			ArrayList<Integer> newindex = (ArrayList<Integer>) index.clone();
			newindex.add(i);
			final Float utility = AiCache.getutility(newindex, ai, cns);
			for (final ChanceNode cn : cns) {
				if (Javelin.DEBUG) {
					// TODO debug
					ActionProvider.checkstacking((BattleState) cn.n);
				}
				final BattleState state = (BattleState) cn.n;
				if (Javelin.DEBUG) {
					// TODO debug
					ActionProvider.checkstacking((BattleState) cn.n);
				}
				final ValueSelector selector =
						state.getBlueTeam().contains(state.next)
								? ai.minValueSelector : ai.maxValueSelector;
				Entry outcomeState = selector.processCurrent(
						new Entry(state, utility, cns), depth, a, b, newindex);
				if (ValueSelector.DEBUGLOG) {
					ValueSelector.LOG
							.append("\n" + ident
									+ (selector == ai.maxValueSelector ? "MAX"
											: "MIN")
									+ (outcomeState.value >= 0 ? "+" : "")
									+ outcomeState.value + "|"
									+ cn.action.replaceAll("\n", ","));
				}
				outcomeState = outcomeState.value == chosen.value ? chosen
						: returnBest(chosen, outcomeState);
				if (outcomeState == chosen) {
					continue;
				}
				if (ValueSelector.DEBUGLOG) {
					ValueSelector.LOG.append("*");
				}
				chosen = new Entry(state, outcomeState.value, cns);
				if (testPod(chosen.value, a, b)) {
					if (ValueSelector.DEBUGLOG) {
						ValueSelector.LOG
								.append(" X (a=" + a + " b=" + b + ")");
					}
					return chosen;
				}
				a = different(a, newAlpha(chosen.value, a));
				b = different(b, newBeta(chosen.value, b));
			}
		}
		return chosen;
	}

	private float different(final float b, final float newBeta) {
		if (ValueSelector.DEBUGLOG && b != newBeta) {
			ValueSelector.LOG.append(" Ą");
		}
		return newBeta;
	}

	/**
	 * @param depth
	 *            Profundidade atual.
	 * @return A identa��o a ser usada no log para esse n�vel da busca.
	 * @author Alex Henry
	 */
	private String calcIdent(final int depth) {
		if (!Javelin.DEBUG) {
			return "";
		}
		String ident = "";

		for (int i = 1; i < depth; i++) {
			ident += " ";
		}

		return ident;
	}

	/**
	 * M�todo utilit�rio.
	 * 
	 * @param previousState
	 *            Estado de jogo a ser usado para a verifica��o.
	 * @param ai
	 *            A busca sendo efetuada.
	 * @param depth
	 *            Profundidade atual.
	 * 
	 * @return <code>true</code>verifica se
	 *         {@link AbstractAlphaBetaSearch#cutoffTest(int) � necess�rio
	 *         realizar poda} ou se foi encontrada um
	 *         {@link AbstractAlphaBetaSearch#terminalTest(Node) t�rmino para o
	 *         jogo}.
	 * 
	 * @author Alex Henry
	 */
	private boolean endOfRecursion(final Entry previousState,
			@SuppressWarnings("rawtypes") final AbstractAlphaBetaSearch ai,
			final int depth) {
		return ai.cutoffTest(depth) || ai.terminalTest(previousState.node);
	}

	/**
	 * Permite que as subclasses atualizem o valor alfa da poda alfabeta.
	 * 
	 * @param current
	 *            O valor utilit�rio da �ltima jogada processada.
	 * @param alpha
	 *            O alpha antes da �ltima jogada ser processada.
	 * @return O valor retornado ser� considerado o valor de alpha ap�s esse
	 *         m�todo ter sido invocado.
	 * @author Alex Henry
	 */
	protected float newAlpha(@SuppressWarnings("unused") final float current,
			final float alpha) {
		// delega �s subclasses
		return alpha;
	}

	/**
	 * Permite que as subclasses atualizem o valor beta da poda alfabeta.
	 * 
	 * @param current
	 *            O valor utilit�rio da �ltima jogada processada.
	 * @param beta
	 *            O beta antes da �ltima jogada ser processada.
	 * @return O valor retornado ser� considerado o valor de beta ap�s esse
	 *         m�todo ter sido invocado.
	 * @author Alex Henry
	 */
	protected float newBeta(@SuppressWarnings("unused") final float current,
			final float beta) {
		// delega �s subclasses
		return beta;
	}

	/**
	 * Delega � subclasse a decis�o de podar ou n�o a busca.
	 * 
	 * @param current
	 *            Valor utilit�rio da �ltima jogada analisada.
	 * @param alpha
	 *            Alpha atual.
	 * @param beta
	 *            Beta atual.
	 * @return Caso a subclasse retorne <code>true</code>, a busca atual ser�
	 *         podada.
	 * @author Alex Henry
	 */
	protected abstract boolean testPod(final float current, final float alpha,
			final float beta);

	/**
	 * Dada uma jogada, a subclasse deve decider qual ser� a rea��o do oponente.
	 * 
	 * @param node
	 *            Jogada anterior.
	 * @param depth
	 *            Profundidade atual da busca.
	 * @param alpha
	 *            Valor alpha atual da busca.
	 * @param beta
	 *            Valor beta atual da busca.
	 * @return A rea��o do oponente.
	 * @author Alex Henry
	 * @param string
	 */
	abstract protected Entry processCurrent(final Entry node, int depth,
			float alpha, final float beta, ArrayList<Integer> newindex);

	/**
	 * A subclasse deve decidir qual das duas jogadas � melhor para ela.
	 * 
	 * @param currentBest
	 *            Jogada.
	 * @param processed
	 *            Jogada.
	 * @return A jogada que n�o foi retornada ser� descartada das poss�veis
	 *         jogadas a serem feitas.
	 * @author Alex Henry
	 */
	abstract protected Entry returnBest(final Entry currentBest,
			final Entry processed);
}