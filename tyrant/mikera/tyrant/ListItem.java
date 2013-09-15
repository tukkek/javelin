package tyrant.mikera.tyrant;

import java.awt.*;

import tyrant.mikera.engine.Thing;


public class ListItem extends Panel {
	private static final long serialVersionUID = 3978147629882421811L;
    private String text;
	
    public ListItem(Thing t) {
		super();
		text = t.getName(Game.hero());
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawString(text, 32, getHeight());
		//int sx=(image%20)*MapPanel.TILEWIDTH;
		//int sy=(image/20)*MapPanel.TILEHEIGHT;
		//g.drawImage(QuestApp.items,0,0,16,16,sx,sy,sx+MapPanel.TILEWIDTH,sy+MapPanel.TILEHEIGHT,null);
	}
}