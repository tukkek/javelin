package javelin.controller.fight;

import javelin.JavelinApp;
import javelin.model.world.Dungeon;

/**
 * {@link Fight} that happens inside a {@link Dungeon}.
 * 
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter {
	@Override
	public int getel(JavelinApp app, int teamel) {
		return super.getel(app, teamel - 4);
	}
}
