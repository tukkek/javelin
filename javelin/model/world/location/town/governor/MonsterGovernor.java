package javelin.model.world.location.town.governor;

import java.util.ArrayList;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.location.town.Dwelling.Draft;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.military.BuildDwelling;
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
		if (rank < season) {
			int a = 1;
		}
		if (rank <= season && start(filter(Growth.class, hand))) {
			return;
		}
		if (rank >= season) {
			if (start(filter(Draft.class, hand))
					|| start(filter(BuildDwelling.class, hand))) {
				return;
			}
		}
		if (rank >= Town.TOWN.rank && town.traits.isEmpty()
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

	public static void raid(Town town) {
		// if (Preferences.DEBUGDISABLECOMBAT) {
		// return;
		// }
		ArrayList<Combatant> incursion = new ArrayList<Combatant>();
		while (town.garrison.size() > 1 && ChallengeRatingCalculator
				.calculateel(incursion) < ChallengeRatingCalculator
						.calculateel(town.garrison)) {
			Combatant c = RPG.pick(town.garrison);
			town.garrison.remove(c);
			incursion.add(c);
		}
		if (!incursion.isEmpty()) {
			Incursion.place(town.realm, town.x, town.y, incursion);
		}
	}
}
