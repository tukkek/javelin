package javelin.controller.ai;

/**
 * An encapsulated {@link StringBuffer}, which may be "turned off" in order to
 * improve performance.
 * 
 * @author Alex Henry
 */
public class OnOffStringBuffer {
	private final StringBuffer sb; // NOPMD

	/**
	 * Construtor.
	 * 
	 * @param write
	 *            If <code>false</code>, this instance will be "dummy" and won't
	 *            do anything at all, and {@link #toString()} shouldn't be
	 *            invoked.
	 *@author Alex Henry
	 */
	public OnOffStringBuffer(final boolean write) {
		sb = write ? new StringBuffer() : null;
	}

	/**
	 * @param s
	 *            If this instance is
	 * 
	 * @author Alex Henry
	 */
	public void append(final String s) {
		if (sb != null) {
			sb.append(s);
		}
	}

	public String toString() {
		if (sb == null) {
			throw new UnsupportedOperationException(
					"This instance has no string to provide.");
		}

		return sb.toString();
	}
}
