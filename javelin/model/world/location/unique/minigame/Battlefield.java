package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.fight.minigame.Battle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.screen.WorldScreen;

/**
 * Large-scale combat minigame.
 * 
 * @author alex
 */
public class Battlefield extends UniqueLocation {
	static final String DESCRIPTION = "Battlefield";

	/** When a {@link Battle} is won, the survivors are kept for recruiting. */
	public HashMap<String, Integer> survivors = new HashMap<String, Integer>();

	/** Constructor. */
	public Battlefield() {
		super(DESCRIPTION, DESCRIPTION, 0, 0);
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// don't
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (survivors.isEmpty()) {
			Javelin.message(
					"No one here.\n\n"
							+ "(Try winning a battlefield match first)\n",
					false);
			return true;
		}
		ArrayList<String> survivors = new ArrayList<String>(
				this.survivors.keySet());
		survivors.sort(null);
		ArrayList<String> choices = new ArrayList<String>(survivors.size());
		ArrayList<Integer> costs = new ArrayList<Integer>(survivors.size());
		for (String survivor : survivors) {
			Integer nsurvivors = this.survivors.get(survivor);
			final Monster m = Javelin.getmonster(survivor);
			int cost = Math.round(m.challengerating * 100) * nsurvivors;
			costs.add(cost);
			choices.add(nsurvivors + " " + survivor + " (" + cost + "XP)");
		}
		int choice = Javelin.choose(
				"You currently have " + Squad.active.sumxp()
						+ " XP.\n\nRecruit which group of survivors?",
				choices, true, false);
		Javelin.app.switchScreen(WorldScreen.active);
		if (choice < 0) {
			return true;
		}
		Integer cost = costs.get(choice);
		if (!Dwelling.canrecruit(cost)) {
			Javelin.message("Not enough experience to recruit this group...",
					false);
			return true;
		}
		recruit(survivors.get(choice), cost);
		return true;
	}

	void recruit(String choice, Integer cost) {
		Dwelling.spend(cost / 100);
		Monster m = Javelin.getmonster(choice);
		for (int i = 0; i < this.survivors.get(choice); i++) {
			Squad.active.members.add(new Combatant(m, true));
		}
		this.survivors.remove(choice);
	}

	@Override
	protected void generate() {
		while (x < 0 || (!Terrain.get(x, y).equals(Terrain.HILL)
				&& !Terrain.get(x, y).equals(Terrain.PLAIN))) {
			super.generate();
		}
	}
}
