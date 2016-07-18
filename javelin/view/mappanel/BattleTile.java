package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.terrain.map.Map;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.Images;

public class BattleTile extends Tile {
	public static final float MAXLIFE = new Float(Combatant.STATUSUNHARMED);

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
			drawcombatant(g, c);
		}
	}

	void drawcombatant(Graphics g, Combatant c) {
		Color active = null;
		boolean blueteam = BattlePanel.state.blueTeam.contains(c);
		if (Game.hero().combatant.equals(c)) {
			active = blueteam ? Color.GREEN : Color.ORANGE;
			g.setColor(active);
			g.fillRect(0, 0, BattlePanel.tilesize, BattlePanel.tilesize);
		}
		draw(g, Images.getImage(c));
		g.setColor(blueteam ? Color.BLUE : Color.RED);
		int hp = BattlePanel.tilesize
				- BattlePanel.tilesize * c.hp / c.getmaxhp();
		// hp = Math.min(hp, BattlePanel.tilesize * 4 / 5);
		// hp = BattlePanel.tilesize * 9 / 10;
		g.fillRect(0, hp, BattlePanel.tilesize / 10, BattlePanel.tilesize - hp);
		if (c.ispenalized(BattlePanel.state)) {
			g.drawImage(
					Images.penalized.getScaledInstance(BattlePanel.tilesize,
							BattlePanel.tilesize, Image.SCALE_DEFAULT),
					0, 0, null);
		}
		if (c.isbuffed()) {
			MapPanelOld.BUFF.paintBorder(this, g, 0, 0, BattlePanel.tilesize,
					BattlePanel.tilesize);
		}
	}

	static void draw(Graphics g, Image gettile) {
		g.drawImage(gettile, 0, 0, MapPanel.tilesize, MapPanel.tilesize, null);
	}
}
