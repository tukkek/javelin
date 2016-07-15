package tyrant.mikera.tyrant;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;

import javelin.controller.old.Game;

import java.awt.Color;

import tyrant.mikera.tyrant.util.Text;


public class DetailedListScreen extends Screen {
	private static final long serialVersionUID = 3257003267744478006L;
    String title = "List";
	String[] strings = null;
	String[] details = null;
	static int page = 0;
	Object result;

	private static int pagesize = 12;
	private static int lineheight = 20;
	private static int leftborder = 20;
	
	private Color selectionColor = Color.yellow;
	private int currentchoice = 0;
	public String bottomString="ESC = Cancel";
	
	public void setSelection( Color c){
		selectionColor = c;
	}

	public Object getObject() {
		boolean end = false;
		while (!end) {
			KeyEvent k = Game.getInput();
			if (k != null) {
				if ( k.getKeyCode() == KeyEvent.VK_ESCAPE )
					return null;

				if( k.getKeyCode() == KeyEvent.VK_ENTER ){
					return strings[currentchoice + page * pagesize];
				}
				
				char c = Character.toLowerCase(k.getKeyChar());
				
				if( c == '8' || k.getKeyCode() == KeyEvent.VK_UP ){
                  if( currentchoice > 0 ){					
					currentchoice--;
					repaint();
                  }
				  return null;	
				}

				if( c == '2' || k.getKeyCode() == KeyEvent.VK_DOWN ){
					currentchoice++;
					if( currentchoice + page * pagesize <  strings.length){
						repaint();
					}else{
						currentchoice--;
					}
				}				
				//Game.warn(Character.toString(c));
				
				if ((c == 'p' || c == '-') && (page > 0)) {
					page--;
					repaint();
					currentchoice = 0;
				}

				if ((c == 'n' || c == '+' || c == ' ')
						&& (((page + 1) * pagesize) < strings.length)) {
					page++;
					repaint();
					currentchoice = 0;
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
	
	public DetailedListScreen(String s, Collection c1 , Collection c2) {
		this(s,getStringArray(c1),getStringArray(c2));
	}
	
	public DetailedListScreen(String s, String[] invstrings, String[] detailstrings) {
		super(Game.getQuestapp());
		setLayout(new GridLayout(15, 1));
		setFont(QuestApp.mainfont);

		addKeyListener(questapp.keyadapter);
		
		// get the strings
		strings = invstrings;
		details = detailstrings;
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
		
		g.drawString(title, leftborder , 1 * lineheight);

		for (int i = 0; i < Math.min(strings.length - (page * pagesize),
				pagesize); i++) {
			String t = strings[i + page * pagesize];
			String s = "[" + (char) ('a' + i) + "] " + Text.properCase( t );
			//Make sure the current selection gets highlighted
			g.setColor( i==currentchoice?selectionColor:QuestApp.INFOTEXTCOLOUR );
			g.drawString(s, 50, lineheight * (i + 2));
		}
		//Make sure that detail and bottom is in proper color
		g.setColor( QuestApp.INFOTEXTCOLOUR );
		//Draw detail string
		//Detail is not allowed to take more than 3 lines for now
		String[] detaillines = Text.wrapString(details[currentchoice], TextZone.linelength);
		for( int i = 0; i < ( detaillines.length  ) ; i++  ){
			g.drawString( detaillines[i] , leftborder , d.height - lineheight * (2 + detaillines.length - i) );
		}
		//Draw bottom string
		String bottomstring = bottomString+" ";
		if (page > 0)
			bottomstring = bottomstring + " Up = Previous ";
		if (((page + 1) * pagesize) < strings.length)
			bottomstring = bottomstring + " Down = Next ";
		g.drawString(bottomstring, leftborder , d.height - lineheight);
	}

}