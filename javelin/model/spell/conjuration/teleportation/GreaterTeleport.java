package javelin.model.spell.conjuration.teleportation;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.Haxor;

/**
 * Teleports {@link Squad#active} to any named {@link Location}.
 * 
 * Currently supports {@link Town} and {@link Haxor}.
 * 
 * @author alex
 */
public class GreaterTeleport extends Spell {
	/**
	 * If <code>true</code> will show each target's {@link Terrain} for a better
	 * informed decision.
	 */
	public boolean showterrain = false;

	/** Constructor. */
	public GreaterTeleport() {
		super("Greater teleport", 7, SpellsFactor.ratespelllikeability(7),
				Realm.MAGIC);
		castinbattle = false;
		castonallies = false;
		castoutofbattle = true;
		isritual = true;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		ArrayList<WorldActor> places = new ArrayList<WorldActor>();
		ArrayList<String> names = new ArrayList<String>();
		for (WorldActor a : WorldActor.getall()) {
			if (a instanceof Town || a instanceof Haxor) {
				places.add(a);
				String town = a.toString();
				if (showterrain) {
					town += " (" + Terrain.get(a.x, a.y) + ")";
				}
				names.add(town);
			}
		}
		WorldActor to =
				places.get(Javelin.choose("Where to?", names, true, true));
		while (WorldActor.get(Squad.active.x, Squad.active.y) != Squad.active) {
			Squad.active.displace();
		}
		Squad.active.place();
		return null;
	}
}
