package javelin.view.mappanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;

public abstract class MapPanel extends Panel {

	// sets current scroll position and repaints map
	public abstract void viewPosition(BattleMap m, int x, int y);

	public abstract void setPosition(BattleMap m, int x, int y);

	// override update to stop flicker TODO
	@Override
	public abstract void update(Graphics g);

	@Override
	public abstract Dimension getPreferredSize();

	// draw area to back buffer
	public abstract void render();

	/**
	 * standard paint method. // - builds map image in back buffer then copies
	 * to screen
	 */
	@Override
	public abstract void paint(Graphics g);

	public abstract void setoverlay(Set<Point> area);

	public abstract void zoom(int factor, boolean save, int x, int y);

	public abstract void autozoom(ArrayList<Combatant> combatants, int x,
			int y);

	abstract public boolean center(int x, int y);

	abstract public void settilesize(int i);

	abstract public void setdiscovered(HashSet<Point> hashSet);

}