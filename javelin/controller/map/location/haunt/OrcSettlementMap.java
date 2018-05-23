package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.old.RPG;
import javelin.view.Images;

public class OrcSettlementMap extends LocationMap {
	public OrcSettlementMap() {
		super("Orc settlement");
		wall = Images.getImage("terrainorcwall");
		flying = false;
	}

	@Override
	public void generate() {
		super.generate();
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map.length; y++) {
				if (RPG.chancein(10) && !nearwall(x, y)) {
					map[x][y].obstructed = true;
				}
			}
		}
	}

	private boolean nearwall(int originx, int originy) {
		for (int deltax = -1; deltax <= +1; deltax++) {
			for (int deltay = -1; deltay <= +1; deltay++) {
				int x = originx + deltax;
				int y = originy + deltay;
				if (validatecoordinate(x, y) && map[x][y].blocked) {
					return true;
				}
			}
		}
		return false;
	}
}
