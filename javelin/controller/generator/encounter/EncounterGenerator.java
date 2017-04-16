package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.Preferences;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.GaveUpException;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Generates an {@link Encounter}.
 * 
 * If I'm not mistaken when manually converting {@link Organization} data on
 * monster.xml to a parseable format I only included monster groups up to 16
 * strong - anything other than that for generated encounters will need to be
 * worked upon or done through other means than only this class.
 * 
 * @author alex
 */
public class EncounterGenerator {
	private static final int MAXSIZEDIFFERENCE = 5;
	static final int MAXTRIES = 1000;

	/**
	 * @param el
	 *            Target encounter level - will work around this is cannot
	 *            generate exactly what is given.
	 * @param terrain
	 *            Usually {@link Terrain#current()} but not necessarily - for
	 *            example not when generation a
	 *            {@link javelin.model.world.location.Location#garrison}, which
	 *            uses the local terrain instead.
	 * @return Enemy units for an encounter.
	 * @throws GaveUpException
	 */
	public static ArrayList<Combatant> generate(int el,
			ArrayList<Terrain> terrains) throws GaveUpException {
		if (Preferences.DEBUGFOE != null) {
			return debugmonster();
		}
		ArrayList<Combatant> encounter = null;
		for (int i = 0; i < MAXTRIES; i++) {
			encounter = select(el, terrains);
			if (encounter == null) {
				continue;
			}
			if (Javelin.app.fight != null
					&& !Javelin.app.fight.validate(encounter)) {
				continue;
			}
			return encounter;
		}
		throw new GaveUpException();
	}

	private static ArrayList<Combatant> debugmonster() {
		ArrayList<Combatant> opponents = new ArrayList<Combatant>();
		if (Preferences.DEBUGFOE != null) {
			Monster m = Javelin.getmonster(Preferences.DEBUGFOE);
			Integer n = Preferences.DEBUGMINIMUMFOES;
			if (n == null) {
				n = 1;
			}
			for (int i = 0; i < n; i++) {
				opponents.add(new Combatant(m.clone(), true));
			}
		}
		return opponents;
	}

	static ArrayList<Combatant> select(int elp, ArrayList<Terrain> terrains) {
		ArrayList<Integer> popper = new ArrayList<Integer>();
		popper.add(elp);
		while (RPG.r(0, 1) == 1) {
			Integer pop = popper.get(RPG.r(0, popper.size() - 1));
			popper.remove(popper.indexOf(pop));
			pop -= 2;
			popper.add(pop);
			popper.add(pop);
		}
		final ArrayList<Combatant> foes = new ArrayList<Combatant>();
		for (final int el : popper) {
			List<Combatant> group = makeencounter(el, terrains);
			if (group == null) {
				return null;
			}
			for (Combatant invitee : group) {
				if (!validatecreature(invitee, foes)) {
					return null;
				}
			}
			for (Combatant invitee : group) {
				foes.add(invitee);
			}
		}
		if (!new MisalignmentDetector(foes).check()) {
			return null;
		}
		if (Preferences.DEBUGMINIMUMFOES != null
				&& foes.size() != Preferences.DEBUGMINIMUMFOES) {
			return null;
		}
		return foes.size() > getmaxenemynumber() ? null : foes;
	}

	private static boolean validatecreature(Combatant invitee,
			ArrayList<Combatant> foes) {
		if (foes.indexOf(invitee) >= 0) {
			return false;
		}
		final String period = Javelin.getDayPeriod();
		final boolean underground = Dungeon.active != null;
		if (invitee.source.nightonly && !underground
				&& (period == Javelin.PERIODMORNING
						|| period == Javelin.PERIODNOON)) {
			return false;
		}
		return true;
	}

	/**
	 * See {@link EncounterGenerator}'s main javadoc description for mote info
	 * on enemy group size.
	 * 
	 * @return The recommended number of enemies to face at most in one battle.
	 *         Other modules may differ from this but this is a suggestion to
	 *         avoid the computer player taking a long time to act while the
	 *         human player has to wait (for example: 1 human unit against 20
	 *         enemies).
	 */
	public static int getmaxenemynumber() {
		return MAXSIZEDIFFERENCE
				+ (Squad.active == null ? 4 : Squad.active.members.size());
	}

	static List<Combatant> makeencounter(final int el,
			ArrayList<Terrain> terrains) {
		List<Encounter> possibilities = new ArrayList<Encounter>();
		int maxel = Integer.MIN_VALUE;
		for (Terrain t : terrains) {
			EncounterIndex index = Organization.ENCOUNTERSBYTERRAIN.get(t.toString());
			if (index != null) {
				maxel = Math.max(maxel, index.lastKey());
				List<Encounter> tier = index.get(el);
				if (tier != null) {
					possibilities.addAll(tier);
				}
			}
		}
		if (el > maxel) {
			return makeencounter(maxel, terrains);
		}
		return possibilities.isEmpty() ? null
				: RPG.pick(possibilities).generate();
	}

	public static ArrayList<Combatant> generate(int el, Terrain terrain)
			throws GaveUpException {
		ArrayList<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);
		return generate(el, terrains);
	}
}
