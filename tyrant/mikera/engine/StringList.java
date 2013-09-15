package tyrant.mikera.engine;

public class StringList {
	private String[] strings = new String[3];
	private int size = 3;
	private int count = 0;
	private static final int delta = 3;

	public void ensureSize(int s) {
		if (s > size) {
			s = s + delta;
			String[] newstrings = new String[s];
			for (int i = 0; i < count; i++)
				newstrings[i] = strings[i];
			size = s;
			strings = newstrings;
		}
	}

	public int getCount() {
		return count;
	}

	public void add(String s) {
		ensureSize(count + 1);
		strings[count] = s;
		count = count + 1;
	}

	public void setString(int i, String s) {
		if ((i < 0) || (i >= count))
			return;
		if (s == null)
			throw new NullPointerException();
		strings[i] = s;
	}

	public String getString(int i) {
		if ((i < 0) || (i >= count))
			return null;
		return strings[i];
	}

	public String getText(String delimiter) {
		if (count <= 0)
			return "";
		StringBuffer sb = new StringBuffer(strings[0]);
		for (int i = 1; i < count; i++) {
			sb.append(delimiter);
			sb.append(strings[i]);
		}
		return sb.toString();
	}

	public void insert(String s, int p) {
		if (s == null)
			throw new NullPointerException();
		ensureSize(count + 1);
		if (p < 0)
			p = 0;
		if (p > count)
			p = count;
		if (count > p)
			System.arraycopy(strings, p, strings, p + 1, count - p);
		strings[p] = s;
		count++;
	}

	public void swap(int a, int b) {
		if ((a >= 0) && (b >= 0) && (a < count) && (b < count)) {
			String s = strings[a];
			strings[a] = strings[b];
			strings[b] = s;
		}
	}

	// sort the strings
	public void sort() {
		boolean end = false;
		while (end == false) {
			end = true;
			for (int i = 0; i < (count - 1); i++) {
				if (strings[i].compareTo(strings[i + 1]) > 0) {
					swap(i, i + 1);
					end = false;
				}
			}
		}
	}

	// compresses identical items and adds count
	public StringList compress() {
		sort();
		StringList s = new StringList();
		if (count <= 0)
			return s;
		int c = 1;
		String d = strings[0];
		for (int i = 1; i < count; i++) {
			if (strings[i].compareTo(d) != 0) {
				s.add((c == 1) ? d : d + " (x" + Integer.toString(c) + ")");
				d = strings[i];
				c = 0;
			}
			c++;
		}
		s.add((c == 1) ? d : d + " (x" + Integer.toString(c) + ")");
		return s;
	}
	
	public StringList compact(int len, String sep) {
		StringList s=new StringList();
		if (count<=0) return s;
		String t=strings[0];
		for (int i=1; i<count; i++) {
			int el=sep.length()+strings[i].length();
			if ((t.length()+el)>len) {
				s.add(t+sep);
				t=strings[i];
			} else {
				t=t+sep+strings[i];
			}
		}
		s.add(t);
		return s;
	}
}