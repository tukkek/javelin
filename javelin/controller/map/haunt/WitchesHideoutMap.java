package javelin.controller.map.haunt;

import javelin.view.Images;

public class WitchesHideoutMap extends HauntMap {
	public WitchesHideoutMap() {
		super("witcheshideout");
		wall = Images.getImage("terrainforest");
		floor = Images.getImage("terrainwoodfloor");
	}
}
