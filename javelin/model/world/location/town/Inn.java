package javelin.model.world.location.town;

import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.Fortification;
import javelin.view.screen.town.PurchaseScreen;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * @author alex
 */
public class Inn extends Fortification {
	private static final int RESTPERIOD = 8;

	public Inn() {
		super("A traveller's lodge", "A traveller's lodge", 1, 5);
		gossip = true;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		int price = Math.round(Math.round(Math.ceil(Squad.active.eat())));
		int weekperiods = 24 * 7 / RESTPERIOD;
		int weekprice = weekperiods * price;
		String prompt = "Do you want to rest at the traveller's inn?\n";
		prompt += "\nENTER to stay ($" + price + "), w to stay for a week ($"
				+ weekprice + ")";
		prompt += "\np to pillage ($" + PurchaseScreen.formatcost(getspoils())
				+ ")";
		prompt += "\nany other key to leave";
		Character input = Javelin.prompt(prompt);
		if (input == '\n') {
			return rest(price, 1);
		}
		if (input == 'w') {
			return rest(weekprice, weekperiods);
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		return false;
	}

	boolean rest(long price, int periods) {
		if (Squad.active.gold < price) {
			Javelin.message("You can't pay the $" + price + " fee!", false);
			return false;
		}
		Squad.active.gold -= price;
		Town.rest(periods, RESTPERIOD * periods, Accommodations.LODGE);
		return true;
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || isnear(Inn.class)) {
			generateawayfromtown();
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}
}
