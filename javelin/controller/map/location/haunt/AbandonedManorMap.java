package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.view.Images;

public class AbandonedManorMap extends LocationMap {
	public AbandonedManorMap() {
		super("Abandoned manor");
		floor = Images.getImage("dungeonfloortempleevil");
		wall = Images.getImage("terrainmoldwall");
		obstacle = Images.getImage("terraintreeforest");
		flying = false;
	}
}
