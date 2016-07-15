package javelin.controller.fight;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.terrain.map.Arena;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.feat.FeatUpgrade;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

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
	public void onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		super.onEnd(screen, originalTeam, s);
		if (!BattleMap.victory) {
			return;
		}
		hall.currentlevel += 1;
		boolean done = hall.currentlevel - 1 >= TrainingHall.EL.length;
		String prefix;
		if (done) {
			prefix = "This has been the final lesson.\n\n";
		} else {
			prefix = "Congratulations, you've graduated this level!\n\n";
		}
		Combatant student = Squad.active.members
				.get(Javelin.choose(prefix + "Which student will learn a feat?",
						Squad.active.members, true, true));
		ArrayList<FeatUpgrade> feats = UpgradeHandler.singleton.getfeats();
		ArrayList<FeatUpgrade> options = new ArrayList<FeatUpgrade>();
		while (options.size() < 3 && !feats.isEmpty()) {
			FeatUpgrade f = RPG.pick(feats);
			feats.remove(f);
			if (!options.contains(f)
					&& f.upgrade(student.clone().clonesource())) {
				options.add(f);
			}
		}
		options.get(Javelin.choose("Learn which feat?", options, true, true))
				.upgrade(student);
		if (done) {
			hall.remove();
		} else {
			hall.generategarrison();
		}
	}
}
