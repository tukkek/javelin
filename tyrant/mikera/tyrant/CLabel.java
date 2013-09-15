package tyrant.mikera.tyrant;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public class CLabel extends Component {
	private static final long serialVersionUID = 3258416114332807730L;
	private String text;

	public CLabel(String text) {
        this.text = text;
    }
    
	public void setText(String text) {
		this.text = text;
		repaint();
	}

	public String getText() {
		return text;
	}
    
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString(text, 20, 0);
    }
    
    public Dimension getMinimumSize() {
        Image image = QuestApp.paneltexture; 
        
        if (image == null) {
            return new Dimension(16, 16);
        }
        return new Dimension(image.getWidth(null), image.getHeight(null));
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

}