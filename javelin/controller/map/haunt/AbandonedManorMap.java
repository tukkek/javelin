package javelin.controller.map.haunt;

import javelin.view.Images;

public class AbandonedManorMap extends HauntMap {
	public AbandonedManorMap() {
		super("Abandoned manor");
		wall = Images.getImage("terraindungeonwall");
		obstacle = Images.getImage("terraintreeforest");
	}
}
