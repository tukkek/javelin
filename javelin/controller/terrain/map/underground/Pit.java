package javelin.controller.terrain.map.underground;

import javelin.model.state.Square;
import tyrant.mikera.engine.RPG;

/**
 * Adapted from {@link tyrant.mikera.tyrant.Pit}.
 * 
 * @author alex
 */
public class Pit extends Caves {
	public Pit() {
		super("Pit");
	}

	@Override
	public void generate() {
		int seeds = SIZE * SIZE / 4;
		while (seeds > 0) {
			Square s = map[RPG.r(0, SIZE - 1)][RPG.r(0, SIZE - 1)];
			if (!s.blocked) {
				s.blocked = true;
				seeds -= 1;
			}
		}
		fractalize(0, 0, (SIZE - 1), (SIZE - 1), 4);
		close();
	}

	/**
	 * fractally builds an area - square technique. very cool algorithm!!
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param gran
	 *            must be a power of 2
	 */
	public void fractalize(final int x1, final int y1, int x2, int y2,
			final int gran) {
		// ensure workable size
		x2 = x1 + (x2 - x1) / gran * gran - 1;
		y2 = y1 + (y2 - y1) / gran * gran - 1;

		final int g = gran / 2;
		if (g < 1) {
			return;
		}
		for (int y = y1; y <= y2; y += gran) {
			for (int x = x1; x <= x2; x += gran) {
				if (RPG.r(2) == 0) {
					map[x + g][y].blocked = map[x][y].blocked;
				} else {
					map[x + g][y].blocked = map[x + gran][y].blocked;
				}
				if (RPG.r(2) == 0) {
					map[x][y + g].blocked = map[x][y].blocked;
				} else {
					map[x][y + g].blocked = map[x][y + gran].blocked;
				}
			}
		}

		// now do middle tile
		for (int y = y1; y <= y2; y += gran) {
			for (int x = x1; x <= x2; x += gran) {
				Square s;
				switch (RPG.d(4)) {
				case 1:
					s = map[x + g][y];
					break;
				case 2:
					s = map[x + g][y + gran];
					break;
				case 3:
					s = map[x][y + g];
					break;
				default:
					s = map[x + gran][y + g];
					break;
				}
				map[x + g][y + g].blocked = s.blocked;
			}
		}

		// continue down to next level of detail
		if (g > 1) {
			fractalize(x1, y1, x2, y2, g);
		}
	}
}
