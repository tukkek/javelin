/*
 * Created on 22-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javelin.controller.old.Game;
import javelin.model.BattleMap;



/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LevelMapPanel extends TPanel {

	private BufferedImage mapBuffer;
	private BattleMap permanentMap=null;
	
	public LevelMapPanel() {
		super(Game.getQuestapp());
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(200,150);
	}
	
	public void setMap(BattleMap m) {
		permanentMap=m;
	}
	
	public void paint(Graphics g) {
		
		BattleMap map;
		if (permanentMap!=null) {
			map=permanentMap;
		} else {
			map=Game.hero().getMap();
		}
		super.paint(g);
		if (map!=null) paintMap(g,map);
	}




	
	public void paintMap(Graphics g,BattleMap map) {
		int w=map.width;
		int h=map.height;
		int[] mem=LevelMap.instance().getMapView(map);
		
		// create back buffer if needed
		if ((mapBuffer == null)||(mapBuffer.getHeight()!=h)||(mapBuffer.getWidth()!=w)) {
			mapBuffer = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		}

		mapBuffer.setRGB(0,0,w,h,mem,0,w);	
		
		g.setColor(Color.BLACK);
		// g.drawRect(0,0,10,10);
		int cx=getWidth()/2;
		int cy=getHeight()/2;
		int cw=w*2;
		int ch=h*2;
		if (cw>cx) {
			ch=(ch*cx)/cw;
			cw=(cw*cx)/cw;
		}
		if (ch>cy) {
			cw=(cw*cy)/ch;
			ch=(ch*cy)/ch;
		}
		try {
			g.drawImage(mapBuffer,cx-cw,cy-ch,cx+cw,cy+ch,0,0,w,h,null);
		} catch(Throwable t) {
			Game.warn(t.toString());
		}
	}
}
