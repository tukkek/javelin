package javelin.controller.terrain.map;

import java.awt.Image;

import javelin.controller.old.Game;
import javelin.model.state.Square;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Portal;
import tyrant.mikera.tyrant.Tile;

/**
 * Adapter that uses a map type from the original tyrant code-base to create a
 * {@link Map}.
 * 
 * @author alex
 */
public class TyrantMap extends Map {
	private final String maptype;

	public TyrantMap(String maptype) {
		super(null);
		this.maptype = maptype;
	}

	public TyrantMap(String maptype, int maxrain) {
		this(maptype);
		maxflooding = maxrain;
	}

	@Override
	public void generate() {
		battlemap = Portal
				.getTargetMap(Game.instance().createWorld().find(maptype));
		for (final Thing t : battlemap.getThings()) {
			inheritance: for (BaseObject i = t.getInherited(); i != null; i =
					i.getInherited()) {
				for (final String s : new String[] { "trap", "ladder", "temple",
						"portal", "secret", "being", "monster", "door" }) {
					if (i.toString().contains(s)) {
						battlemap.removeThing(t);
						break inheritance;
					}
				}
			}
		}

		map = new Square[battlemap.width][battlemap.height];
		for (int x = 0; x < battlemap.width; x++) {
			for (int y = 0; y < battlemap.height; y++) {
				map[x][y] = new Square(battlemap.isBlocked(x, y),
						battlemap.getObjects(x, y) != null,
						battlemap.getTile(x, y) == Tile.POOL);
			}
		}
	}

	@Override
	public Image getblockedtile(int x, int y) {
		if (map[x][y].blocked && map[x][y].obstructed) {
			return floor;
		}
		return wall;
	}
}
