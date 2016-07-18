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

/**
 * TODO remove
 * 
 * @author alex
 */
public abstract class MapPanelCommon extends Panel {

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

	public abstract void setoverlay(Set<Point> area);

	public abstract void zoom(int factor, boolean save, int x, int y);

	public abstract void autozoom(ArrayList<Combatant> combatants, int x,
			int y);

	abstract public boolean center(int x, int y, boolean b);

	abstract public void settilesize(int i);

	abstract public void setdiscovered(HashSet<Point> hashSet);

	public abstract void refresh();

	public abstract void init();

}