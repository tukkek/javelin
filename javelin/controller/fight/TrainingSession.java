package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.Weather;
import javelin.controller.terrain.map.Arena;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.BattleScreen;

/**
 * A {@link Fight} that happens inside the {@link TrainingHall}.
 * 
 * @author alex
 */
public class TrainingSession extends Siege {
	TrainingHall hall;

	/** See {@link Siege#Siege(Location)}. */
	public TrainingSession(TrainingHall hall) {
		super(hall);
		this.hall = hall;
		friendly = true;
		rewardgold = false;
		bribe = false;
		hide = false;
		cleargarrison = false;
		map = new Arena();
		map.maxflooding = Weather.DRY;
	}

	@Override
	public boolean onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		super.onEnd(screen, originalTeam, s);
		if (Fight.victory) {
			hall.level();
		}
		return true;
	}
}
