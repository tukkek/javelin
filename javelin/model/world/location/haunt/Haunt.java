package javelin.model.world.location.haunt;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.HauntFight;
import javelin.controller.map.haunt.HauntMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.unique.Haxor;
import tyrant.mikera.engine.RPG;

public abstract class Haunt extends Fortification {
	ArrayList<Monster> dwellers = new ArrayList<Monster>();

	/**
	 * @param description
	 *            Location name.
	 * @param tier
	 *            A number from 1 to 4 (inclusive), determining the level range
	 *            for this haunt (1-5, 6-10, 11-15, 16-20).
	 */
	public Haunt(String description, String[] monsters) {
		super(description, description, Integer.MAX_VALUE, Integer.MIN_VALUE);
		realm = null;
		for (String name : monsters) {
			Monster m = Javelin.getmonster(name);
			dwellers.add(m);
			int cr = Math.max(1, Math.round(m.challengerating));
			if (cr < minlevel) {
				minlevel = cr;
			}
			if (cr > maxlevel) {
				maxlevel = cr;
			}
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	@Override
	protected HauntFight fight() {
		HauntFight f = new HauntFight(this);
		f.map = getmap();
		return f;
	}

	abstract HauntMap getmap();

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		int minel = ChallengeRatingCalculator.leveltoel(minlevel);
		int maxel = ChallengeRatingCalculator.leveltoel(maxlevel);
		int el = Integer.MIN_VALUE;
		List<List<Combatant>> possibilities = new ArrayList<List<Combatant>>();
		while (el < maxel) {
			garrison.add(new Combatant(RPG.pick(dwellers).clone(), true));
			el = ChallengeRatingCalculator.calculateel(garrison);
			if (minel <= el && el <= maxel) {
				possibilities.add(new ArrayList<Combatant>(garrison));
			}
		}
		if (possibilities.isEmpty()) {
			generategarrison(minlevel, maxlevel);
		} else {
			garrison = RPG.pick(possibilities);
		}
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		Javelin.message("You find a ruby inside the now-safe "
				+ descriptionknown.toLowerCase() + "!", true);
		Haxor.singleton.rubies += 1;
		remove();
		return true;
	}
}
