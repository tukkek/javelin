package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.terrain.map.Map;
import javelin.model.state.Meld;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.Images;

public class BattleTile extends Tile {
	public static final float MAXLIFE = new Float(Combatant.STATUSUNHARMED);

	public static MapPanel panel = null;
	private Image obstacle;
	public boolean shrouded;

	public BattleTile(final int xp, final int yp, final boolean discoveredp,
			final MapPanel panel) {
		super(xp, yp, discoveredp);
		setSize(MapPanel.tilesize, MapPanel.tilesize);
		addMouseListener(panel.mouse);
		shrouded = !discovered;
	}

	@Override
	public void paint(final Graphics g) {
		if (!discovered) {
			return;
		}
		final Map m = Javelin.app.fight.map;
		final Square s = BattlePanel.state.map[x][y];
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
		final Combatant c = BattlePanel.state.getCombatant(x, y);
		if (c != null) {
			drawcombatant(g, c, this);
		} else if (Javelin.app.fight.meld) {
			for (Meld meld : BattlePanel.state.meld) {
				if (meld.x == x && meld.y == y) {
					draw(g, meld.getimage(BattlePanel.state));
				}
			}
		}
		if (shrouded) {
			g.setColor(new Color(0, 0, 0, 0.8f));
			g.fillRect(0, 0, BattlePanel.tilesize, BattlePanel.tilesize);
		}
		if (BattlePanel.overlay != null) {
			BattlePanel.overlay.overlay(this, g);
		}
	}

	static private void drawcombatant(final Graphics g, final Combatant c,
			final Tile t) {
		final boolean blueteam = BattlePanel.state.blueTeam.contains(c);
		if (Game.hero().combatant.equals(c)) {
			g.setColor(blueteam ? Color.GREEN : Color.ORANGE);
			g.fillRect(0, 0, BattlePanel.tilesize, BattlePanel.tilesize);
		}
		draw(g, Images.getImage(c));
		g.setColor(blueteam ? Color.BLUE : Color.RED);
		final int hp = BattlePanel.tilesize
				- BattlePanel.tilesize * c.hp / c.getmaxhp();
		g.fillRect(0, hp, BattlePanel.tilesize / 10, BattlePanel.tilesize - hp);
		if (c.ispenalized(BattlePanel.state)) {
			g.drawImage(
					Images.penalized.getScaledInstance(BattlePanel.tilesize,
							BattlePanel.tilesize, Image.SCALE_DEFAULT),
					0, 0, null);
		}
		if (c.isbuffed()) {
			MapPanelOld.BUFF.paintBorder(t, g, 0, 0, BattlePanel.tilesize,
					BattlePanel.tilesize);
		}
	}

	private static void draw(final Graphics g, final Image gettile) {
		g.drawImage(gettile, 0, 0, MapPanel.tilesize, MapPanel.tilesize, null);
	}
}
