package javelin.model.world.location.fortification;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Accommodations;
import javelin.model.world.location.town.Town;
import javelin.view.screen.town.PurchaseScreen;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * @author alex
 */
public class Inn extends Fortification {
	public Inn() {
		super("A traveller's lodge", "A traveller's lodge", 1, 5);
		gossip = true;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		long price = Math.round(Math.ceil(Squad.active.eat()));
		if (Squad.active.gold < price) {
			Javelin.prompt("You can't pay the $" + price + " fee!");
			return false;
		}
		String prompt = "Do you want to rest at the traveller's inn for $"
				+ price + "?" + "\n";
		prompt += "\nENTER to stay";
		prompt += "\np to pillage ($" + PurchaseScreen.formatcost(getspoils())
				+ ")";
		prompt += "\nAny other key to leave";
		Character input = Javelin.prompt(prompt);
		if (input == '\n') {
			Squad.active.gold -= price;
			Town.rest(1, 8, Accommodations.LODGE);
			return true;
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		return false;
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || iscloseto(Inn.class)) {
			generateawayfromtown();
		}
	}
}
