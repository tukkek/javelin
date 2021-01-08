package javelin.controller.content.action;

/**
 * Describes an action.
 *
 * @author alex
 */
public interface ActionDescription{

	/**
	 * @return Keys as strings.
	 */
	String[] getDescriptiveKeys();

	/**
	 * @return Action name.
	 */
	String getDescriptiveName();

	/**
	 * @return Usually the first of {@link #getDescriptiveKeys()}.
	 */
	String getMainKey();

	/**
	 * @param key Replaces the key given by {@link #getMainKey()}.
	 */
	void setMainKey(String key);

}