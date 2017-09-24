package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.WitchesHideoutMap;
import javelin.controller.terrain.Terrain;

public class WitchesHideout extends Haunt {
	public WitchesHideout() {
		super("Witches' hideout",
				new String[] { "Green hag", "Harpy", "Succubus" });
	}

	@Override
	LocationMap getmap() {
		return new WitchesHideoutMap();
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || !Terrain.FOREST.equals(Terrain.get(x, y))) {
			super.generate();
		}
	}
}
