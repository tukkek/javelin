package javelin.old.underground;

import java.util.ArrayList;
import java.util.Collections;

import javelin.model.state.Square;
import javelin.old.RPG;

/**
 * @author alex
 */
public class Maze extends Caves {
	private static final float POKERATIO = 1 / 4f;

	/** Constructor. */
	public Maze() {
		super("Maze");
		coresize = 0;
	}

	@Override
	public void generate() {
		init();
		int center = (SIZE + 1) / 2;
		map[2 * RPG.r(center)][2 * RPG.r(center)].blocked = false;
		int finishedCount = 0;
		for (int i = 1; i < center * center * 1000
				&& finishedCount < center * center; i++) {
			int x = 0 + 2 * RPG.r(center);
			int y = 0 + 2 * RPG.r(center);
			if (!map[x][y].blocked) {
				continue;
			}
			int dx = RPG.r(1, 2) == 1 ? RPG.r(2) * 2 - 1 : 0;
			int dy = dx == 0 ? RPG.r(2) * 2 - 1 : 0;
			int lx = x + dx * 2;
			int ly = y + dy * 2;
			if (lx >= 0 && lx <= SIZE - 1 && ly >= 0 && ly <= SIZE - 1) {
				if (!map[lx][ly].blocked) {
					map[x][y].blocked = false;
					map[x + dx][y + dy].blocked = false;
					finishedCount++;
				}
			}
		}
		poke();
		close();
	}

	void poke() {
		ArrayList<Square> walls = new ArrayList<>();
		for (Square[] squares : map) {
			for (Square s : squares) {
				if (s.blocked) {
					walls.add(s);
				}
			}
		}
		Collections.shuffle(walls);
		float pokes = walls.size() * POKERATIO;
		for (int i = 0; i < pokes; i++) {
			walls.get(i).blocked = false;
		}

	}

}
