package javelin.view.mappanel;

import java.awt.Graphics;
import java.awt.Image;

import javelin.Javelin;
import javelin.controller.terrain.map.Map;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.Images;

public class BattleTile extends Tile {
	public static MapPanel panel = null;
	private Image obstacle;

	public BattleTile(int xp, int yp, MapPanel panel) {
		super(xp, yp);
		setSize(MapPanel.tilesize, MapPanel.tilesize);
		addMouseListener(panel.mouse);
	}

	@Override
	public void paint(Graphics g) {
		Map m = Javelin.app.fight.map;
		Square s = BattlePanel.state.map[x][y];
		if (!s.blocked) {
			draw(g, m.floor);
			if (s.obstructed) {
				if (obstacle == null) {
					obstacle = m.getobstacle();
				}
				draw(g, obstacle);
			}
		} else if (m.wallfloor != null) {
			draw(g, m.wallfloor);
			draw(g, m.wall);
		} else {
			draw(g, m.floor);
			draw(g, m.wall);
		}
		if (s.flooded) {
			draw(g, m.flooded);
		}
		Combatant c = BattlePanel.state.getCombatant(x, y);
		if (c != null) {
			draw(g, Images.getImage(c));
		}
	}

	static void draw(Graphics g, Image gettile) {
		g.drawImage(gettile, 0, 0, MapPanel.tilesize, MapPanel.tilesize, null);
	}
}
