package javelin.model.world.location;

import java.util.List;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.LairFight;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * A battle in which the final opponent can be captured as a {@link Squad}
 * member.
 * 
 * @author alex
 */
public class Lair extends Location {
	/** Constructor. */
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

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}
}
