package javelin.model.world.location.town.governor;

import java.util.ArrayList;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
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

	/**
	 * TODO
	 * 
	 * @author alex
	 */
	class Draft extends Labor {
		Draft(String name, int cost) {
			super(name, cost);
		}

		@Override
		protected void define() {
			// TODO Auto-generated method stub

		}

		@Override
		public void done() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean validate(District d) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	/** Constructor. */
	public MonsterGovernor(Town t) {
		super(t);
	}

	@Override
	public void manage() {
		// System.out.println("implement mosnter manager!"); // TODO
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
			// TODO// pick(filter(BuildDwelling.class, hand));
		}
		if (town.getrank() - 1 >= Town.Rank.TOWN.ordinal()
				&& town.traits.isEmpty()) {
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
}
