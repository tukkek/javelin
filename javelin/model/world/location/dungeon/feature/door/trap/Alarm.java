package javelin.model.world.location.dungeon.feature.door.trap;

import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.door.Door;

public class Alarm extends DoorTrap {
	public static final DoorTrap INSTANCE = new Alarm();

	private Alarm() {
		// prevent instantiation
	}

	@Override
	public void generate(Door d) {
		// activated on opening
	}

	@Override
	public void activate(Combatant opening) {
		Javelin.message("Opening the door causes a loud noise!", false);
		throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
	}
}
