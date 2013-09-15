package javelin.controller.exception;

// TODO change into compile time exception
public class RepeatTurnException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
