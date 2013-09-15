
package tyrant.mikera.tyrant.util;

import java.io.*;
import java.util.StringTokenizer;

import tyrant.mikera.tyrant.Portal;


/** 
* Class contining utility functions for text manipulations
*/
public class Text {
    public static String NL = System.getProperty("line.separator");


	// return Roman numerals
	public static String roman(int n) {
		String r = "";
		switch (n / 1000) {
			case 1 :
				r += "M";
				break;
			case 2 :
				r += "MM";
				break;
			case 3 :
				r += "MMM";
				break;
		}
		n = n % 1000;

		switch (n / 100) {
			case 1 :
				r += "C";
				break;
			case 2 :
				r += "CC";
				break;
			case 3 :
				r += "CCC";
				break;
			case 4 :
				r += "CD";
				break;
			case 5 :
				r += "D";
				break;
			case 6 :
				r += "DC";
				break;
			case 7 :
				r += "DCC";
				break;
			case 8 :
				r += "DCCC";
				break;
			case 9 :
				r += "CM";
				break;
		}
		n = n % 100;

		switch (n / 10) {
			case 1 :
				r += "X";
				break;
			case 2 :
				r += "XX";
				break;
			case 3 :
				r += "XXX";
				break;
			case 4 :
				r += "XL";
				break;
			case 5 :
				r += "L";
				break;
			case 6 :
				r += "LX";
				break;
			case 7 :
				r += "LXX";
				break;
			case 8 :
				r += "LXXX";
				break;
			case 9 :
				r += "XC";
				break;
		}
		n = n % 10;

		switch (n) {
			case 1 :
				r += "I";
				break;
			case 2 :
				r += "II";
				break;
			case 3 :
				r += "III";
				break;
			case 4 :
				r += "IV";
				break;
			case 5 :
				r += "V";
				break;
			case 6 :
				r += "VI";
				break;
			case 7 :
				r += "VII";
				break;
			case 8 :
				r += "VIII";
				break;
			case 9 :
				r += "IX";
				break;
		}

		return r;
	}

	public static String ordinal(int n) {
        String st=Integer.toString(n);
        if (((n % 100) >= 11) && ((n % 100) <= 13)) {
            return st + "th";
        } else if (n % 10 == 1) {
            return st + "st";
        } else if (n % 10 == 2) {
            return st + "nd";
        } else if (n % 10 == 3) {
            return st + "rd";
        } else {
            return st + "th";
        }
	}
	
	// return index of string s in array ss
	public static int index(String s, String[] ss) {
		for (int i = 0; i < ss.length; i++) {
			if (s.equals(ss[i]))
				return i;
		}
		return -1;
	}

	private static final String whitespace = "                                                                                          ";
	// return whitesapce of specified length
	
	public static String whiteSpace(int l) {
		if (l > 0)
			return whitespace.substring(0, l);
        return "";
	}

	public static String leftPad(String s, int l) {
		if (s==null) s="";
		return whiteSpace(l-s.length())+s;
	}
	
	public static String rightPad(String s, int l) {
		if (s==null) s="";
		return s+whiteSpace(l-s.length());
	}
	
	// returns a+whitesapce+b with total length len
	public static String centrePad(String a, String b, int len) {
		len = len - a.length();
		len = len - b.length();
		return a + whiteSpace(len) + b;
	}

	public static String capitalise(String s) {
		if (s==null) return null;
		if (s.length()==0) return "";
		char c = s.charAt(0);
		if (Character.isUpperCase(c))
			return s;
		StringBuffer sb = new StringBuffer(s);
		sb.setCharAt(0, Character.toUpperCase(c));
		return sb.toString();
	}

	public static String titleCase(String s) {
		StringBuffer sb = new StringBuffer(s);
		for (int i=0; i<s.length(); i++) {
			if ((i==0)||(!Character.isLetterOrDigit(s.charAt(i-1)))) {
				sb.setCharAt(i, Character.toUpperCase(s.charAt(i)));
			}
		}
		return sb.toString();
	}
	
	public static int countChar(String s, char c) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c)
				count++;
		}
		return count;
	}

	public static int wrapLength(String s, int start, int len) {
		if ((s.length() - start) <= len)
			return (s.length() - start);

		for (int i = len; i >= 0; i--) {
			if (Character.isWhitespace(s.charAt(i + start)))
				return i;
		}

		return len;
	}

	public static String loadFromFile(String name) {
        StringBuffer sb=new StringBuffer();
    	
	    try {
	    	InputStream in=Portal.class.getResourceAsStream(name);
	    	
	    	BufferedReader br=new BufferedReader(new InputStreamReader(in));
	    	for ( String s=br.readLine(); s!=null; s=br.readLine()) {
		        sb.append(s);
		        sb.append(NL);
		    }
	    } catch (Throwable t) {
        	t.printStackTrace();
        	return null;
        }
        
		return sb.toString();
	}
	
	public static String[] wrapString(String s, int len) {
		String[] working = new String[1 + ((2 * s.length()) / len)];

		int end = s.length();
		int pos = 0;
		int i = 0;
		while (pos < end) {
			int inc = wrapLength(s, pos, len);
			working[i] = s.substring(pos, pos + inc);
			i++;
			pos = pos + inc;
			while ((pos < end) && Character.isWhitespace(s.charAt(pos)))
				pos++;
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

	public static int pond(String s) {
		// mildly tricky encryption hash
		// can't be bothered with anything tougher yet
		int len = s.length();

		int a = 0;
		int f = 1;
		for (int i = len - 1; i >= 0; i--) {
			a += s.charAt(i) * f;
			f *= 31;
		}

		return a;
	}

	public static String[] separateString(String s, char c) {
		int num = countChar(s, c);
		int start = 0;
		int finish = 0;
		String[] result = new String[num + 1];
		for (int i = 0; i < (num + 1); i++) {
			finish = s.indexOf(c, start);
			if (finish < start)
				finish = s.length();
			if (start < finish) {
				result[i] = s.substring(start, finish);
			} else {
				result[i] = "";
			}
			start = finish + 1;
		}
		return result;
	}

	public static boolean isVowel(char c) {
		c = Character.toLowerCase(c);
		return ((c == 'a') || (c == 'e') || (c == 'i') || (c == 'o') || (c == 'u'));
    }

    /**
     * Convert a string with embedded spaces into proper camel notation.
     @param input a string to camelize.
     @return the newly camelized string.
    */
    public static String camelizeString(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input," ");
        StringBuffer output = new StringBuffer();
        String token = null;
        while (tokenizer.hasMoreElements()) {
            token = tokenizer.nextToken();
            char first = token.charAt(0);
            if (Character.isLetter(first)) {
                output.append(Character.toUpperCase(first));
                output.append(token.substring(1));
            } else {
                output.append(token);
            }
        }
        return output.toString();
    }

    /**
     * Convert a string with embedded spaces into proper case notation.
     @param input a string to properCase.
     @return the newly properCased string.
    */    
    
    public static String properCase(String input) {
      StringTokenizer tokenizer = new StringTokenizer(input," ");
      StringBuffer output = new StringBuffer();
      String token = null;
      while (tokenizer.hasMoreElements()) {
          token = tokenizer.nextToken();
          char first = token.charAt(0);
          if (Character.isLetter(first)) {
              output.append(Character.toUpperCase(first));
              output.append(token.substring(1));
          } else {
              output.append(token);
          }
          if( tokenizer.hasMoreElements() ){
            output.append(" ");
          }
      }
      return output.toString();
  }
    
    
    
	/**
	 * Converts a string into the appropriate Object for Tyrant properties
	 * 
	 * @param s
	 * @return
	 */
    public static Object parseObject(String s) {
    	s=s.trim();
    	if (Character.isDigit(s.charAt(0))) {
    		try { 	
	    		if (s.indexOf(".")>=1) {
	    			return new Double(Double.parseDouble(s));
	    		} 
	    			
	    		return new Integer(Integer.parseInt(s));
	    		
	    	} catch (Throwable t) {
	    		// safe catch
	    	}
    	}
    	return s;
    }
            
}