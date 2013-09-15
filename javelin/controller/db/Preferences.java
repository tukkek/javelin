package javelin.controller.db;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Preferences {
	private static final String BUNDLE_NAME = "preferences"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Preferences() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	static public Integer getInteger(String key, Integer fallback) {
		String value = getString(key);
		if (value == null) {
			return fallback;
		} else {
			return Integer.parseInt(value);
		}
	}
}
