//
// A simple class to display the title image
//
package tyrant.mikera.tyrant;

import java.awt.Graphics;
import java.awt.Image;

public class TitleScreen extends Screen {

	public TitleScreen(QuestApp q) {
		super(q);
	}
	
	private static final long serialVersionUID = 3904958651298164789L;

    public void paint(Graphics g) {
		super.paint(g);
		Image im = QuestApp.title;
		g.drawImage(im, (getBounds().width - im.getWidth(null)) / 2,
				(getBounds().height - im.getHeight(null)) / 2, null);

	}

}