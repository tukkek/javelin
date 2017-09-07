package javelin.controller.action.world;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.WorldGenerator;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * TODO This should probably take a week to build but this just encourages the
 * player to split the settler manually, which is boring. Maybe use some sort of
 * construction site here?
 * 
 * @see Work
 * @author alex
 */
public class BuildTown extends WorldAction {

	public BuildTown() {
		super("Build town", new int[0], new String[] { "b" });
	}

	@Override
	public void perform(WorldScreen screen) {
		Combatant settler = null;
		for (Combatant c : Squad.active.members) {
			if (c.source.name.equals("Settler")) {
				settler = c;
				break;
			}
		}
		if (settler == null) {
			Javelin.message("You need a settler to build a town.", false);
			return;
		}
		if (Javelin.prompt("Are you sure you want to build a new town here?\n\n"
				+ "Press ENTER to confirm or any other key to cancel...") != '\n') {
			return;
		}
		Squad.active.members.remove(settler);
		Point location = new Point(Squad.active.x, Squad.active.y);
		Town t = new Town(location, WorldGenerator.determinecolor(location).realm);
		t.description = "your new town";
		t.rename();
		t.garrison.clear();
		Squad.active.displace();
	}
}