package javelin.controller.map.haunt;

import javelin.model.state.Square;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

public class ShatteredTempleMap extends HauntMap {
	public ShatteredTempleMap() {
		super("Shattered temple");
		floor = Images.getImage("dungeonfloortempleevil");
		wall = Images.getImage("dungeonwall");
		obstacle = Images.getImage("terrainbush");
	}

	@Override
	Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (!s.blocked && RPG.r(1, 6) == 1) {
			s.obstructed = true;
		}
		return s;
	}
}
