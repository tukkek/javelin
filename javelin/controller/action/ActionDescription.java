package javelin.controller.action;

/**
 * Describes an action.
 * 
 * @author alex
 */
public interface ActionDescription {

	/**
	 * @return Keys as strings.
	 */
	abstract String[] getDescriptiveKeys();

	/**
	 * @return Action name.
	 */
	abstract String getDescriptiveName();

	/**
	 * @return Usually the first of {@link #getDescriptiveKeys()}.
	 */
	abstract String getMainKey();

	/**
	 * @param key
	 *            Replaces the key given by {@link #getMainKey()}.
	 */
	void setMainKey(String key);

}