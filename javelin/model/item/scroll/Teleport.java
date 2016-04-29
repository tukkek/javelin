package javelin.model.item.scroll;

import java.util.ArrayList;

import javelin.controller.action.world.CastSpells;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.WorldScreen;

/**
 * Teleports {@link Squad#active} to any named {@link WorldPlace}.
 * 
 * Currently supports {@link Town} and {@link Haxor}.
 * 
 * @author alex
 */
public class Teleport extends Scroll {

	public Teleport() {
		super("Scroll of greater teleport", 2275, Item.MAGIC, 7,
				SpellsFactor.ratespelllikeability(7));
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		ArrayList<WorldActor> places = new ArrayList<WorldActor>();
		ArrayList<String> names = new ArrayList<String>();
		for (WorldActor a : WorldScreen.getactors()) {
			if (a instanceof Town || a instanceof Haxor) {
				places.add(a);
				names.add(a.toString());
			}
		}
		WorldActor to =
				places.get(CastSpells.choose("Where to?", names, true, true));
		Squad.active.visual.remove();
		Squad.active.x = to.x;
		Squad.active.y = to.y;
		while (WorldScreen.getactor(Squad.active.x,
				Squad.active.y) != Squad.active) {
			Squad.active.displace();
		}
		Squad.active.place();
		return true;
	}

}
