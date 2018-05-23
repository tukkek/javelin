package javelin.old;

import java.awt.event.KeyEvent;

import javelin.view.screen.BattleScreen;

public class Interface {
	public KeyEvent keyevent;
	private Object lock = new Object();
	/** If <code>true</code> is waiting for a {@link BattleScreen} input. */
	public boolean waiting = false;;

	public void go(KeyEvent k) {
		keyevent = k;
		waiting = false;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void getInput() {
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			waiting = false;
		}
	}
}