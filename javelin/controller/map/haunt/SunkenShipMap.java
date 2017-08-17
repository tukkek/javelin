package javelin.controller.map.haunt;

import javelin.controller.Point;
import javelin.model.state.Square;
import javelin.view.Images;

public class SunkenShipMap extends HauntMap {
	public SunkenShipMap() {
		super("Sunken ship");
		floor = Images.getImage("terraintraininghall");
		flooded = Images.getImage("terrainaquatic");
	}

	@Override
	Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (c == '3') {
			s.flooded = true;
			startingareared.add(new Point(x, y));
		}
		return s;
	}
}
