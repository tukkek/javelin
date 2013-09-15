package tyrant.mikera.tyrant;

import java.awt.event.KeyEvent;

public class Interface {
	protected KeyEvent keyevent;
	private Object lock = new Object();
    
	public void go(KeyEvent k) {
		keyevent = k;
		synchronized(lock) {
        	lock.notifyAll();
        }
	}

	public void getInput() {
		try {
			synchronized(lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
            e.printStackTrace();
		}
	}
}