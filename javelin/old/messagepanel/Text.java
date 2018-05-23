package javelin.old.messagepanel;

/**
 * Class contining utility functions for text manipulations
 */
public class Text {

	public static int countChar(String s, char c) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	public static int wrapLength(String s, int start, int len) {
		if (s.length() - start <= len) {
			return s.length() - start;
		}

		for (int i = len; i >= 0; i--) {
			if (Character.isWhitespace(s.charAt(i + start))) {
				return i;
			}
		}

		return len;
	}

	public static String[] wrapString(String s, int len) {
		String[] working = new String[1 + 2 * s.length() / len];

		int end = s.length();
		int pos = 0;
		int i = 0;
		while (pos < end) {
			int inc = wrapLength(s, pos, len);
			working[i] = s.substring(pos, pos + inc);
			i++;
			pos = pos + inc;
			while (pos < end && Character.isWhitespace(s.charAt(pos))) {
				pos++;
			}
		}

		// return empty string if size zero
		if (i == 0) {
			i = 1;
			working[0] = "";
		}

		String[] result = new String[i];
		System.arraycopy(working, 0, result, 0, i);
		return result;
	}

	public static String[] separateString(String s, char c) {
		int num = countChar(s, c);
		int start = 0;
		int finish = 0;
		String[] result = new String[num + 1];
		for (int i = 0; i < num + 1; i++) {
			finish = s.indexOf(c, start);
			if (finish < start) {
				finish = s.length();
			}
			if (start < finish) {
				result[i] = s.substring(start, finish);
			} else {
				result[i] = "";
			}
			start = finish + 1;
		}
		return result;
	}
}