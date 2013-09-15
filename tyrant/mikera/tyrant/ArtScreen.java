package tyrant.mikera.tyrant;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;


public class ArtScreen extends Screen {
	private static final long serialVersionUID = 3257568408080953650L;
    String title = "List";
	Thing[] arts = null;
	static int page = 0;

	private static int pagesize = 13;
	private static int lineheight = 20;

	public Thing getArt() {
		while (true) {
			KeyEvent k = Game.getInput();

			if (k.getKeyCode() == KeyEvent.VK_ESCAPE)
				return null;

			char c = Character.toLowerCase(k.getKeyChar());

			if ((c == 'p') && (page > 0)) {
				page--;
				repaint();
			}

			if ((c == 'n' || c == ' ')
					&& (((page + 1) * pagesize) < arts.length)) {
				page++;
				repaint();
			}

			int kv = c - 'a';
			if ((kv >= 0) && (kv < pagesize)) {
				kv = kv + page * pagesize;
				if ((kv >= 0) && (kv < arts.length))
					return arts[kv];
			}
		}
	}

	public ArtScreen(String s, Thing[] thearts) {
		super(Game.getQuestapp());
		setLayout(new GridLayout(15, 1));

		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
		setFont(QuestApp.mainfont);

		// get the things
		arts = thearts;
		if (arts == null)
			arts = new Thing[0];

		// set the list title
		title = s;
	}

	public void paint(Graphics g) {
		if ((page * pagesize) > arts.length)
			page = 0;

		g.drawString(title, 20, 1 * lineheight);

		for (int i = 0; i < Math.min(arts.length - (page * pagesize), pagesize); i++) {
			Thing t = arts[i + page * pagesize];
			String s = "[" + (char) ('a' + i) + "] " + t.getName(Game.hero());

			g.drawString(s, 50, lineheight * (i + 2));
		}

		String bottomstring = "ESC=Cancel ";
		if (page > 0)
			bottomstring = bottomstring + " P=Previous ";
		if (((page + 1) * pagesize) < arts.length)
			bottomstring = bottomstring + " N=Next ";
		String powerstring = "Power: " + Game.hero().getStat(RPG.ST_MPS) + " / "
				+ Game.hero().getStat(RPG.ST_MPSMAX);
		g.drawString(Text.centrePad(bottomstring, powerstring, 55), 20,
				getSize().height - 10);
	}

}