package javelin.controller.exception;

public class BattleEvent extends RuntimeException {
	@Override
	public synchronized Throwable fillInStackTrace() {
		/* default implemntation is very inneficient */
		return this;
	}
}
