/**
 * Alex Henry on 27/09/2009
 */
package javelin.controller.ai;

import java.io.Serializable;

/**
 * {@link Exception Exce��o} simples. � usada quando o tempo m�ximo permitido
 * para a {@link AbstractAlphaBetaSearch busca minimax} � esgotado.
 * 
 * @author Alex Henry
 */
public class TimeUpException extends Exception {
	/**
	 * Vers�o do objeto {@link Serializable serializ�vel}.
	 * 
	 * @author Alex Henry
	 */
	private static final long serialVersionUID = 1L;
}