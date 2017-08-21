package javelin.controller.map.haunt;

import javelin.model.state.Square;
import javelin.view.Images;
import tyrant.mikera.engine.RPG;

public class GraveyardMap extends HauntMap {
	public GraveyardMap() {
		super("graveyard");
		wall = Images.getImage("terraintombstone");
		obstacle = Images.getImage("terrainbush");
		floor = Images.getImage("dungeonfloortempleevil");
	}

	@Override
	Square processtile(int x, int y, char c) {
		Square s = super.processtile(x, y, c);
		if (!s.blocked && RPG.chancein(20)) {
			s.obstructed = true;
		}
		return s;
	}
}
