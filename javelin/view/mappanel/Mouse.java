package javelin.view.mappanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.old.Interface;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

public abstract class Mouse extends MouseAdapter{
	MapPanel panel;

	public Mouse(MapPanel panel){
		this.panel=panel;
	}

	@Override
	public void mouseClicked(MouseEvent e){
		if(e.getButton()==MouseEvent.BUTTON3){
			Tile t=gettile(e);
			BattleScreen.active.mappanel.center(t.x,t.y,true);
		}
	}

	protected Tile gettile(MouseEvent e){
		int x=Math.floorDiv(e.getX(),MapPanel.tilesize);
		int y=Math.floorDiv(e.getY(),MapPanel.tilesize);
		return panel.tiles[x][y];
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e){
		Point p=JavelinApp.context==null?BattlePanel.current.getlocation()
				:JavelinApp.context.getherolocation();
		panel.zoom(-e.getWheelRotation(),false,p.x,p.y);
	}

	/**
	 * @return Subclasses should call this at the beggining of
	 *         {@link #mouseClicked(MouseEvent)} and return without further action
	 *         if <code>true</code>.
	 */
	public boolean overrideinput(){
		if(Interface.userinterface.waiting) return false;
		final KeyEvent k=new KeyEvent(BattleScreen.active.mappanel,0,
				System.currentTimeMillis(),0,0,'c');
		k.setKeyChar('\n');
		Interface.userinterface.go(k);
		return true;
	}
}
