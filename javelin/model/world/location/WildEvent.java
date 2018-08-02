package javelin.model.world.location;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.event.wild.WildEventCard;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;

/**
 * A mostly ad-hoc (scaled) type of Location meant to make the regions of the
 * game {@link World} more interesting to explore regardless of a
 * {@link Squad}'s current level. It also adds variety to the rest of the mostly
 * {@link Fight}-oriented encounters with lighter strategic decision making and
 * subquests.
 *
 * @see WildEventCard
 * @author alex
 */
public class WildEvent extends Location {
	static final String DESCRIPTION = "A point of interest";
	WildEventCard card = null;

	/** Constructor. */
	public WildEvent() {
		super(DESCRIPTION);
		discard = false;
		allowedinscenario = false;
		impermeable = true;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (card == null) {
			int el = ChallengeCalculator.calculateel(Squad.active.members);
			card = WildEventCard.generate(Squad.active, el, this);
		}
		card.happen(Squad.active, this);
		if (card.remove) {
			remove();
		}
		return true;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public Integer getel(int attackerel) {
		return null;
	}
}
