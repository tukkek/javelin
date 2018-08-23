package javelin.controller.map.location.haunt;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.view.Images;

public class SunkenShipMap extends LocationMap {
	public SunkenShipMap() {
		super("Sunken ship");
		floor = Images.get("terrainshipfloor");
		flooded = Images.get("terrainaquatic");
	}

	@Override
	protected Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (c == '3') {
			s.flooded = true;
			startingareared.add(new Point(x, y));
		}
		return s;
	}
}
