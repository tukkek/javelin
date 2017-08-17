package javelin.controller.map.haunt;

import javelin.view.Images;

public class AbandonedManorMap extends HauntMap {
	public AbandonedManorMap() {
		super("Abandoned manor");
		floor = Images.getImage("dungeonfloortempleevil");
		wall = Images.getImage("terrainmoldwall");
		obstacle = Images.getImage("terraintreeforest");
	}
}
