package javelin.model.world.location.town.governor;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.comparator.CombatantByCr;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.basic.Dwelling.BuildDwelling;
import javelin.model.world.location.town.labor.basic.Dwelling.Draft;
import javelin.model.world.location.town.labor.basic.Growth;
import tyrant.mikera.engine.RPG;

/**
 * Governor for hostile towns.
 *
 * TODO here should go all the code for generating monster garissons and squads
 *
 * @see Town#ishostile()
 * @author alex
 */
public class MonsterGovernor extends Governor {

	/** Constructor. */
	public MonsterGovernor(Town t) {
		super(t);
		nprojects = 2;
	}

	@Override
	public void manage() {
		ArrayList<Labor> hand = new ArrayList<Labor>();
		int tries = 0;
		while (hand.size() != gethand().size()) {
			tries += 1;
			if (tries >= 5) {
				break;
			}
			for (Labor l : gethand()) {
				if (l.automatic) {
					if (!hand.contains(l)) {
						hand.add(l);
					}
				} else {
					l.discard();
				}
			}
		}
		selectcards(hand);
		// draft(hand);
		// if (hand.isEmpty()) {
		// gethand().clear();
		// redraw();
		// // manage();
		// } else {
		// selectcards(hand);
		// }
	}

	void selectcards(ArrayList<Labor> hand) {
		ArrayList<Labor> traits = filter(Trait.class, hand);
		hand.removeAll(traits);
		long season = getseason();
		int rank = town.getrank().rank;
		// if (rank < season) {
		// int a = 1;
		// }
		if (rank <= season && start(filter(Growth.class, hand))) {
			return;
		}
		if (rank >= season) {
			if (start(filter(Draft.class, hand))
					|| start(filter(BuildDwelling.class, hand))) {
				return;
			}
		}
		if (rank >= Rank.TOWN.rank && town.traits.isEmpty()
				&& startttrait(traits)) {
			return;
		}
		if (getprojectssize() < nprojects) {
			if (start(hand) || startttrait(traits)) {
				return;
			}
		}
	}

	boolean startttrait(ArrayList<Labor> traits) {
		if (traits.isEmpty()) {
			return false;
		}
		Labor trait = RPG.pick(traits);
		trait.start();
		return true;
	}

	void draft(ArrayList<Labor> hand) {
		// TODO Auto-generated method stub
	}

	boolean start(ArrayList<Labor> cards) {
		Labor l = pick(cards);
		if (l == null) {
			return false;
		}
		l.start();
		return true;
	}

	ArrayList<Labor> filter(Class<? extends Labor> cardtype,
			ArrayList<Labor> hand) {
		ArrayList<Labor> found = new ArrayList<Labor>();
		for (Labor l : hand) {
			if (cardtype.isInstance(l)) {
				found.add(l);
			}
		}
		return found;
	}

	public static void raid(Town t) {
		if (t.garrison.size() < 2) {
			return;
		}
		int el = CrCalculator.calculateel(t.garrison);
		if (el <= t.population
				|| t.garrison.size() <= Math.min(30, t.population)) {
			return;
		}
		List<Combatant> garrison = new ArrayList<Combatant>(t.garrison);
		garrison.sort(CombatantByCr.SINGLETON);
		List<Combatant> incursion = new ArrayList<Combatant>(
				garrison.subList(0, garrison.size() / 2));
		if (!incursion.isEmpty()) {
			t.garrison.removeAll(incursion);
			Incursion.place(t.realm, t.x, t.y, incursion);
		}
	}

	@Override
	protected void always(ArrayList<Labor> hand) {
		if (town.getrank().rank <= getseason()) {
			start(filter(Growth.class, hand));
		}
	}
}
