package javelin.model.unit.abilities.spell.conjuration.teleportation;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.ActorsByName;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.UniqueLocation;

/**
 * Teleports {@link Squad#active} to any named {@link Location}.
 *
 * @author alex
 */
public class GreaterTeleport extends Spell {
	/**
	 * If <code>true</code> will show each target's {@link Terrain} for a better
	 * informed decision. Assumes level 20 to be able to move as many creatures
	 * as desired.
	 *
	 * @see WordOfRecall
	 */
	public boolean showterrain = false;

	/** Constructor. */
	public GreaterTeleport() {
		super("Greater teleport", 7,
				ChallengeCalculator.ratespelllikeability(7, 20), Realm.MAGIC);
		casterlevel = 20;
		castinbattle = false;
		castonallies = false;
		castoutofbattle = true;
		isritual = true;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		ArrayList<Actor> places = new ArrayList<>();
		for (Actor a : World.getactors()) {
			if (!World.seed.discovered.contains(a.getlocation())) {
				continue;
			}
			if (a instanceof Town || a instanceof UniqueLocation
					|| a instanceof Temple) {
				places.add(a);
			}
		}
		places.sort(ActorsByName.INSTANCE);
		Actor to = places.get(Javelin.choose("Where to?", places, true, true));
		WordOfRecall.teleport(to.x, to.y);
		return null;
	}
}
