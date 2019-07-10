package javelin.view.mappanel.world;

import java.awt.Graphics;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class WorldPanel extends MapPanel{

	static final HashMap<Point,Actor> ACTORS=new HashMap<>();
	public static final HashMap<Point,Location> DESTINATIONS=new HashMap<>();

	public WorldPanel(){
		super(World.getseed().map.length,World.getseed().map[0].length,
				Preferences.KEYTILEWORLD);
	}

	@Override
	protected Mouse getmouselistener(){
		return new WorldMouse(this);
	}

	@Override
	protected int gettilesize(){
		return Preferences.tilesizeworld;
	}

	@Override
	protected Tile newtile(int x,int y){
		return new WorldTile(x,y,this);
	}

	@Override
	public void paint(Graphics g){
		updateactors();
		super.paint(g);
	}

	void updateactors(){
		DESTINATIONS.clear();
		ACTORS.clear();
		for(Actor a:World.getactors()){
			ACTORS.put(new Point(a.x,a.y),a);
			if(!(a instanceof Location)) continue;
			Location l=(Location)a;
			if(l.link) DESTINATIONS.put(new Point(l.x,l.y),l);
		}
	}

	@Override
	public void refresh(){
		updateactors();
		for(Tile[] ts:tiles)
			for(Tile t:ts)
				t.repaint();
	}

	@Override
	public void repaint(){
		/*
		 * For some reasone super.repaint() isn't calling #paint at all, so
		 * let's do it manually
		 */
		super.repaint();
	}

	@Override
	public void setup(){
		super.setup();
		scroll.setVisible(false);
	}
}
