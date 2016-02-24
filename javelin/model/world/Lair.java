package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.LairFight;
import tyrant.mikera.engine.RPG;

/**
 * A battle in which the final opponent can be captured as a {@link Squad}
 * member.
 * 
 * @author alex
 */
public class Lair extends WorldPlace implements WorldActor {
	public static List<WorldPlace> lairs = new ArrayList<WorldPlace>();

	public Lair() {
		super("dungeon", "a lair");
	}

	@Override
			List<WorldPlace> getall() {
		return lairs;
	}

	@Override
	public void enter() {
		super.enter();
		throw new StartBattle(new LairFight());
	}

	/**
	 * TODO Would be interesting being able to spend time on the pub and perhaps
	 * hear about the location of dungeons or incursions
	 */
	static public void spawn(float chance) {
		if (RPG.random() < chance) {
			new Lair().place();
		}
	}
}
