package javelin.controller.exception;

public class NotPeaceful extends Throwable {
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
