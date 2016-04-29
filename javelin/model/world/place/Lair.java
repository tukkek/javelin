package javelin.model.world.place;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.LairFight;
import javelin.model.world.Squad;

/**
 * A battle in which the final opponent can be captured as a {@link Squad}
 * member.
 * 
 * TODO Would be interesting being able to spend time on the pub and perhaps
 * hear about the location of dungeons or incursions
 * 
 * @author alex
 */
public class Lair extends WorldPlace {
	public Lair() {
		super("A lair");
	}

	@Override
	public boolean interact() {
		if (super.interact()) {
			throw new StartBattle(new LairFight());
		}
		return false;
	}

	@Override
	protected Integer getel(int attackerel) {
		return null;
	}
}
