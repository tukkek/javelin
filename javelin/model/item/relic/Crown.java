package javelin.model.item.relic;

import javelin.Javelin;
import javelin.model.Realm;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Knowledgeable;

/**
 * Gives +5 to each skill for a week (all-knowing condition)
 * 
 * @author alex
 */
public class Crown extends Relic {

	/** Constructor. */
	public Crown() {
		super("Crown of Knowlege", Realm.WATER);
		usedinbattle = false;
		usedoutofbattle = true;
	}

	@Override
	protected boolean activate(Combatant user) {
		user.addcondition(new Knowledgeable(user));
		Javelin.message(user + " becomes knowledgeable", false);
		return true;
	}
}
