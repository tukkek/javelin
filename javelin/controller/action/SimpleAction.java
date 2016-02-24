package javelin.controller.action;

/**
 * Basic description for an action that can be used both in and out of battle.
 * 
 * @see ActionAdapter
 * 
 * @author alex
 */
public interface SimpleAction {
	void perform();

	int[] getkeys();

	String getname();
}
