package javelin.controller.exception;

import javelin.controller.ai.AiThread;
import javelin.controller.ai.ThreadManager;

/**
 * Throw when the time limit is up for {@link ThreadManager}.
 * 
 * @see AiThread
 * 
 * @author alex
 */
public class StopThinking extends RuntimeException {
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}