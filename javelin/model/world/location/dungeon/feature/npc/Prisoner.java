package javelin.model.world.location.dungeon.feature.npc;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

public class Prisoner extends Inhabitant {
	public Prisoner(int xp, int yp) {
		super(xp, yp, Dungeon.active.level + Difficulty.VERYEASY + 1,
				Dungeon.active.level + Difficulty.EASY);
	}

	@Override
	public boolean activate() {
		MasterKey key = Squad.active.equipment.get(MasterKey.class);
		String message = "There is a prisoner here (" + inhabitant + ").\n";
		if (key == null) {
			message += "If you had a master key you could free him...";
			Javelin.message(message, false);
			return false;
		}
		message += "Do you want to free him with your master key?\n"
				+ "Press f to free prisoner, any other key to cancel...";
		if (Javelin.prompt(message, false) != 'f') {
			return false;
		}
		if (RPG.chancein(2)) {
			Javelin.message("You are interrupted by a group of guards!", false);
			throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
		}
		Squad.active.equipment.remove(key);
		Combatant prisoner = Javelin.recruit(inhabitant.source);
		prisoner.hp = prisoner.maxhp
				* RPG.r(Combatant.STATUSWOUNDED, Combatant.STATUSSCRATCHED)
				/ Combatant.STATUSUNHARMED;
		Dungeon.active.features.remove(this);
		return true;
	}
}