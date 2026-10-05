package javelin.controller.content.action;

/**
 * Basic description for an action that can be used both in and out of battle.
 *
 * @see ActionAdapter
 *
 * @author alex
 */
public interface SimpleAction{
	/** Executes action. */
	void perform();

	/**
	 * @return {@link Integer} codes for keys.
	 */
	int[] getcodes();

	/**
	 * @return Action name.
	 */
	String getname();

	/**
	 * @return Text keys.
	 */
	String[] getkeys();
}
