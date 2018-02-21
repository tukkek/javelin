package javelin.controller.fight.minigame.arena.building;

import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.shopping.ShoppingScreen;

public class ArenaShop extends ArenaBuilding {
	public ArenaShop() {
		super("Shop", "locationshop",
				"Click this shop to buy items for the active unit!");
	}

	@Override
	protected boolean click(Combatant current) {
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current)
				+ "\n\nYour gladiators currently have $"
				+ ShoppingScreen.formatcost(ArenaFight.get().gold) + ".";
	}
}
