package javelin.controller.action;

/**
 * Describes an action.
 * 
 * @author alex
 */
public interface ActionDescription {

	abstract String[] getDescriptiveKeys();

	abstract String getDescriptiveName();

	abstract String getMainKey();

	void setMainKey(String key);

}