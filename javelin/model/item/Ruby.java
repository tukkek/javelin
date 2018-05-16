package javelin.model.item;

import java.util.ArrayList;

import javelin.controller.action.world.UseItems;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.wish.WishScreen;

public class Ruby extends Item {
	public Ruby() {
		super("Wish ruby", 0, null);
		consumable = true;
		waste = false;
		usedinbattle = false;
	}

	@Override
	public void register() {
		// don't
	}

	@Override
	public void expend() {
		// spent elsewhere
	}

	@Override
	public boolean usepeacefully(Combatant user) {
		Squad.active.equipment.clean();
		int rubies = 0;
		for (ArrayList<Item> bag : Squad.active.equipment.values()) {
			for (Item i : bag) {
				if (i instanceof Ruby) {
					rubies += 1;
				}
			}
		}
		new WishScreen(rubies).show();
		UseItems.skiperror = true;
		return false;
	}
}
