package javelin.old;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TPanel extends Panel{
	protected Image buffer;

	public TPanel(){
		super();
		setFocusable(true);
		setBackground(QuestApp.PANELCOLOUR);
		addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				Interface.userinterface.go(e);
			}
		});
		setFocusTraversalKeysEnabled(false);
	}

	// update with double buffering.
	@Override
	public void update(Graphics g){
		// graphics context for the buffer
		Graphics bg;
		// only build when needed
		if(buffer==null||buffer.getWidth(this)!=this.getSize().width
				||buffer.getHeight(this)!=this.getSize().height)
			try{
				buffer=this.createImage(getSize().width,getSize().height);
			}catch(Throwable x){
				return;
			}
		bg=buffer.getGraphics();
		// paint the panel
		paint(bg);
		super.paint(bg);
		g.drawImage(buffer,0,0,this);
	}

	@Override
	public void paint(Graphics g){
		Rectangle bounds=getBounds();
		int width=bounds.width;
		int height=bounds.height;

		Image texture=QuestApp.DEFAULTTEXTURE;
		int twidth=texture.getWidth(null);
		int theight=texture.getHeight(null);
		for(int lx=0;lx<width;lx+=twidth)
			for(int ly=0;ly<height;ly+=theight)
				g.drawImage(texture,lx,ly,null);
		/*
		 * This is required for contained lightweight components to be rendered.
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