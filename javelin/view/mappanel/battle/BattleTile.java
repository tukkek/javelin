package javelin.view.mappanel.battle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaBuilding;
import javelin.controller.map.Map;
import javelin.model.state.Meld;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class BattleTile extends Tile {
	public static final float MAXLIFE = new Float(Combatant.STATUSUNHARMED);
	static final Border BUFF = BorderFactory.createLineBorder(Color.WHITE);

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
		final Square s = Fight.state.map[x][y];
		if (!s.blocked) {
			if (m.floor == null) { // TODO remove after testing
				throw new RuntimeException("No floor for map " + m.getClass());
			}
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
		final Combatant c = Fight.state.getcombatant(x, y);
		if (c != null) {
			drawcombatant(g, c, this);
		} else {
			for (Meld meld : Fight.state.meld) {
				if (meld.x == x && meld.y == y) {
					draw(g, meld.getimage(Fight.state));
				}
			}
		}
		if (shrouded) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(0, 0, MapPanel.tilesize, MapPanel.tilesize);
		}
		if (MapPanel.overlay != null) {
			MapPanel.overlay.overlay(this, g);
		}
	}

	static private void drawcombatant(final Graphics g, final Combatant c,
			final Tile t) {
		final boolean isblueteam = Fight.state.blueTeam.contains(c);
		if (BattlePanel.current.equals(c)) {
			g.setColor(isblueteam ? Color.GREEN : Color.ORANGE);
			g.fillRect(0, 0, MapPanel.tilesize, MapPanel.tilesize);
		}
		draw(g, Images.getImage(c));
		g.setColor(isblueteam ? Color.BLUE : Color.RED);
		final int hp = MapPanel.tilesize
				- MapPanel.tilesize * c.hp / c.getmaxhp();
		g.fillRect(0, hp, MapPanel.tilesize / 10, MapPanel.tilesize - hp);
		if (c.ispenalized(Fight.state)) {
			final Image penalized = Images.PENALIZED.getScaledInstance(
					MapPanel.tilesize, MapPanel.tilesize, Image.SCALE_DEFAULT);
			g.drawImage(penalized, 0, 0, null);
		}
		if (c.isbuffed()) {
			BUFF.paintBorder(t, g, 0, 0, MapPanel.tilesize, MapPanel.tilesize);
		}
		if (c.elite) {
			final Image elite = Images.ELITE.getScaledInstance(
					MapPanel.tilesize, MapPanel.tilesize, Image.SCALE_DEFAULT);
			g.drawImage(elite, 0, 0, null);
		}
		if (c.mercenary) {
			final Image mercenary = Images.MERCENARY.getScaledInstance(
					MapPanel.tilesize, MapPanel.tilesize, Image.SCALE_DEFAULT);
			g.drawImage(mercenary, 0, 0, null);
		} else if (c.summoned) {
			final Image summoned = Images.SUMMONED.getScaledInstance(
					MapPanel.tilesize, MapPanel.tilesize, Image.SCALE_DEFAULT);
			g.drawImage(summoned, 0, 0, null);
		} else if (c instanceof ArenaBuilding
				&& ((ArenaBuilding) c).repairing) {
			final Image summoned = Images.LABOR.getScaledInstance(
					MapPanel.tilesize, MapPanel.tilesize, Image.SCALE_DEFAULT);
			g.drawImage(summoned, 0, 0, null);
		}
	}
}
