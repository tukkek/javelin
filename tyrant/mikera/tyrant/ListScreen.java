package tyrant.mikera.tyrant;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;

public class ListScreen extends Screen {
	private static final long serialVersionUID = 3257003267744478006L;
    String title = "List";
	String[] strings = null;
	static int page = 0;
	Object result;

	private static int pagesize = 26;
	private static int lineheight = 20;
	public String bottomString="ESC = Cancel";

	public Object getObject() {
		boolean end = false;
		while (!end) {
			KeyEvent k = Game.getInput();
			if (k != null) {
				if ((k.getKeyCode() == KeyEvent.VK_ESCAPE)||(k.getKeyCode()==KeyEvent.VK_ENTER))
					return null;

				char c = Character.toLowerCase(k.getKeyChar());

	            if (k.getKeyCode() == KeyEvent.VK_UP) c = '-';
	            if (k.getKeyCode() == KeyEvent.VK_DOWN) c = '+';
	            
				//Game.warn(Character.toString(c));
				
				if ((c == 'p' || c == '-') && (page > 0)) {
					page--;
					repaint();
				}

				if ((c == 'n' || c == '+' || c == ' ')
						&& (((page + 1) * pagesize) < strings.length)) {
					page++;
					repaint();
				}

				int kv = c - 'a';
				if ((kv >= 0) && (kv < pagesize)) {
					kv = kv + page * pagesize;
					if ((kv >= 0) && (kv < strings.length))
						return strings[kv];
				}
			}
		}
		return null;
	}

	public void activate() {
		// questapp.setKeyHandler(keyhandler);
	}

	private static String[] getStringArray(Collection c) {
		String[] ss=new String[c.size()];
		Iterator it=c.iterator();
		int i=0;
		while (it.hasNext()) {
			ss[i++]=(String)it.next();
		}
		return ss;
	}
	
	public ListScreen(String s, Collection c) {
		this(s,getStringArray(c));
	}
	
	public ListScreen(String s, String[] invstrings) {
		super(Game.getQuestapp());
		setLayout(new GridLayout(15, 1));
		setFont(QuestApp.mainfont);

		addKeyListener(questapp.keyadapter);
		
		// get the strings
		strings = invstrings;
		if (strings == null)
			strings = new String[0];

		// set the list title
		title = s;
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		if ((page * pagesize) > strings.length)
			page = 0;

		Dimension d = getSize();

		g.setColor(QuestApp.INFOTEXTCOLOUR);
		
		g.drawString(title, 20, 1 * lineheight);

		for (int i = 0; i < Math.min(strings.length - (page * pagesize),
				pagesize); i++) {
			String t = strings[i + page * pagesize];
			String s = "[" + (char) ('a' + i) + "] " + t;
			g.drawString(s, 50, lineheight * (i + 2));
		}

		String bottomstring = bottomString+" ";
		if (page > 0)
			bottomstring = bottomstring + " Up = Previous ";
		if (((page + 1) * pagesize) < strings.length)
			bottomstring = bottomstring + " Down = Next ";
		g.drawString(bottomstring, 20, d.height - 20);
	}

}