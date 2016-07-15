package javelin.controller.terrain.map.tyrant;

import javelin.controller.Weather;
import javelin.controller.terrain.map.TyrantMap;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.TempleDungeon;
import javelin.view.Images;

public class Caves extends TyrantMap {
	public Caves() {
		super("caves", Weather.DRY);
		if (Dungeon.active instanceof TempleDungeon) {
			floor = Images.getImage(Dungeon.active.floor);
			wall = Images.getImage(Dungeon.active.wall);
		} else {
			floor = Images.getImage("terraindungeonfloor");
			wall = Images.getImage("terraindungeonwall");
		}
	}
}
