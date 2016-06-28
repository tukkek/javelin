package javelin.controller.terrain.map.tyrant;

import javelin.controller.Weather;
import javelin.controller.terrain.map.TyrantMap;
import javelin.view.Images;

public class Caves extends TyrantMap {
	public Caves() {
		super("caves", Weather.DRY);
		floor = Images.getImage("terraindungeonfloor");
		wall = Images.getImage("terraindungeonwall");
	}
}
