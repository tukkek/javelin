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
	}

	@Override
	public void manage() {
		ArrayList<Labor> hand = new ArrayList<Labor>();
		for (Labor l : gethand()) {
			if (l.automatic) {
				hand.add(l);
			} else {
				l.discard();
			}
		}
		draft(hand);
		if (hand.isEmpty()) {
			gethand().clear();
			redraw();
			// manage();
		} else {
			selectcards(hand);
		}
	}

	void selectcards(ArrayList<Labor> hand) {
		ArrayList<Labor> traits = filter(Trait.class, hand);
		hand.removeAll(traits);
		start(filter(Growth.class, hand));
		if (!start(filter(Draft.class, hand))) {
			pick(filter(BuildDwelling.class, hand));
		}
		if (town.getrank().rank >= Town.TOWN.rank && town.traits.isEmpty()) {
			startttrait(traits);
		}
		if (getprojectssize() == 0 && !start(hand)) {
			startttrait(traits);
		}
	}

	void startttrait(ArrayList<Labor> traits) {
		if (!traits.isEmpty()) {
			Labor trait = RPG.pick(traits);
			trait.start();
		}
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
