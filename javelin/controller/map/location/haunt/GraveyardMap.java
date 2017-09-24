package javelin.controller.map.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.model.state.Square;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

public class GraveyardMap extends LocationMap {
	public GraveyardMap() {
		super("graveyard");
		wall = Images.getImage("terraintombstone");
		obstacle = Images.getImage("terrainbush");
		floor = Images.getImage("dungeonfloortempleevil");
	}

	@Override
	protected Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (!s.blocked && RPG.chancein(20)) {
			s.obstructed = true;
		}
		return s;
	}
}
