package javelin.view.mappanel.dungeon;

import javelin.controller.db.Preferences;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldPanel;

public class DungeonPanel extends MapPanel {
	Dungeon dungeon;

	public DungeonPanel(Dungeon d) {
		super(d.size, d.size, Preferences.KEYTILEDUNGEON);
		this.dungeon = d;
	}

	@Override
	protected Mouse getmouselistener() {
		return new DungeonMouse(this);
	}

	@Override
	protected int gettilesize() {
		return Preferences.TILESIZEDUNGEON;
	}

	@Override
	protected Tile newtile(int x, int y) {
		return new DungeonTile(x, y, this);
	}

	@Override
	public void init() {
		super.init();
		scroll.setVisible(false);
	}

	@Override
	public void refresh() {
		if (initial) {
			WorldPanel.resize(this, Dungeon.active.herolocation.x,
					Dungeon.active.herolocation.y);
			scroll.setVisible(true);
		}
		super.refresh();
		repaint();
	}

	@Override
	public void repaint() {
		// super.repaint();
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				if (t.discovered) {
					t.repaint();
				}
			}
		}
	}

	// @Override
	// protected void ensureminimumsize() {
	// /* don't */
	// /*
	// * TODO would be cool to have larger dungeons to make use of the entire
	// * screen
	// */
	// }
}
