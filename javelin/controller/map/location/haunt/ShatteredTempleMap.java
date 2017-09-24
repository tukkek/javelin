package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

public class ShatteredTempleMap extends LocationMap {
	public ShatteredTempleMap() {
		super("Shattered temple");
		floor = Images.getImage("dungeonfloortempleevil");
		wall = Images.getImage("terrainrockwall2");
		obstacle = Images.getImage("terrainbush");
	}

	@Override
	protected Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (!s.blocked && RPG.r(1, 6) == 1) {
			s.obstructed = true;
		}
		return s;
	}
}
