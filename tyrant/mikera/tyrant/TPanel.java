package tyrant.mikera.tyrant;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;

import javelin.controller.old.Game;

public class TPanel extends Panel {
    public QuestApp questapp;
	
	private static final long serialVersionUID = 3257289145112998709L;

    public TPanel(QuestApp q) {
        super();
        setFocusable(true);
        setBackground(QuestApp.PANELCOLOUR);
        
        globalKeyListenerHasBeenAdded = true;
    
        if (q!=null) {
	        questapp=q;
	        
	        addKeyListener(questapp.keyadapter);
	    }
    }
    

    public Image getTexture() {
		return QuestApp.paneltexture; 
	}
  
	protected Image buffer;
    protected boolean globalKeyListenerHasBeenAdded;

	// update with double buffering. 
	public void update(Graphics g) {
		// graphics context for the buffer
		Graphics bg; 
    
		// only build when needed
		if ((buffer==null) || (buffer.getWidth(this) != this.getSize().width) || (buffer.getHeight(this) != this.getSize().height)) {
	    	try {
	    		buffer = this.createImage(getSize().width, getSize().height);
	    	} catch(Throwable x) {
	    		Game.warn("TPanel.update() exception");
	    		return;
	    	}
		}

		bg = buffer.getGraphics();

		// paint the panel
		paint(bg);
		super.paint(bg);
    
		g.drawImage(buffer, 0, 0, this);
	}

  public void paint(Graphics g) {
    Rectangle bounds=getBounds();
    int width=bounds.width;
    int height=bounds.height;
    
    Image texture=getTexture();
    if (texture!=null) {
    	int twidth=texture.getWidth(null);
    	int theight=texture.getHeight(null);
    	for (int lx=0; lx<width; lx+=twidth) {
    		for (int ly=0; ly<height; ly+=theight) {
    			g.drawImage(texture,lx,ly,null); 
    		} 
    	}
    }

    /* This is required for contained lightweight components to be rendered.
     * Like MessagePanel and TextZone.
     */
    super.paint(g);
    
    g.setColor(QuestApp.PANELHIGHLIGHT);
    g.fillRect(0,0,width,2);
    g.fillRect(0,0,2,height);
    g.setColor(QuestApp.PANELSHADOW);
    g.fillRect(1,height-2,width-1,2);
    g.fillRect(width-2,1,2,height-1);
    
  }
} 