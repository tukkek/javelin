package javelin.controller.db.reader.factor;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.encounter.Encounter;
import javelin.controller.encounter.EncounterGenerator;
import javelin.controller.encounter.EncounterPossibilities;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.place.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * @see FieldReader
 * @see #process()
 */
public class Organization extends FieldReader {
	class EncounterData {
		final ArrayList<String> queue = new ArrayList<String>();
	}

	/**
	 * Possible encounters by terrain type.
	 */
	public final static HashMap<String, EncounterIndex> ENCOUNTERS =
			new HashMap<String, EncounterIndex>();
	static ArrayList<EncounterData> data = new ArrayList<EncounterData>();

	public Organization(final MonsterReader reader, final String fieldname) {
		super(reader, fieldname);
	}

	@Override
	public void read(final String value)
			throws NumberFormatException, PropertyVetoException {
		final String[] frequencies = value.split(",");
		for (final String frequency : frequencies) {
			final String[] encounter = frequency.split("plus");
			final EncounterData group = new EncounterData();
			group.queue.add(encounter[0] + " " + reader.monster.name);
			for (int i = 1; i < encounter.length; i++) {
				group.queue.add(encounter[i]);
			}
			data.add(group);
		}
	}

	static ArrayList<String> terrain() {
		ArrayList<String> terrains = new ArrayList<String>();
		if (Dungeon.active != null) {
			terrains.add("underground");
			return terrains;
		}
		terrains.add(Javelin.terrain(Javelin.terrain()));
		if (Weather.now == 2) {
			terrains.add("aquatic");
		}
		return terrains;
	}

	/**
	 * Uses organization data to create {@link Encounter}s.
	 */
	static public void process() {
		final HashMap<String, Monster> monstersbyname =
				new HashMap<String, Monster>();
		for (Monster m : Javelin.ALLMONSTERS) {
			monstersbyname.put(m.toString().toLowerCase(), m);
		}
		inform("\n== Encounters==");
		for (final EncounterData group : data) {
			for (final Encounter e : prepareexpansion(group.queue,
					monstersbyname)) {
				if (e.size() <= EncounterGenerator.MAXGROUPSIZE) {
					register(e);
				}
			}
		}
		if (Javelin.DEBUG) {
			inform("");
			inform("");
			int i = 0;
			for (String terrain : ENCOUNTERS.keySet()) {
				for (int el : ENCOUNTERS.get(terrain).keySet()) {
					for (Encounter e : ENCOUNTERS.get(terrain).get(el)) {
						inform("    " + terrain + " EL" + el + ": " + e);
						i += 1;
					}
					inform("");
				}
			}
			inform("\nTotal encounters: " + i);
		}
		/* Mark for garbage collection */
		data = null;
	}

	public static void register(final Encounter e) {
		final HashSet<String> terrains = new HashSet<String>();
		jointerrains(e.group, terrains);
		for (final String terrain : terrains) {
			EncounterIndex index = ENCOUNTERS.get(terrain);
			if (index == null) {
				index = new EncounterIndex();
				ENCOUNTERS.put(terrain, index);
			}
			index.put(e.rate(), e);
		}
	}

	public static void jointerrains(List<Combatant> group,
			final HashSet<String> terrains) {
		for (final Combatant c : group) {
			for (final String terrain : c.source.terrains) {
				terrains.add(terrain);
			}
		}
	}

	private static List<Encounter> prepareexpansion(ArrayList<String> queue,
			HashMap<String, Monster> monstersbyname) {
		final ArrayList<EncounterPossibilities> list =
				new ArrayList<EncounterPossibilities>();
		for (String group : queue) {
			final EncounterPossibilities p = new EncounterPossibilities();
			group = group.trim();
			int separator = group.indexOf(' ');
			final String name =
					group.substring(separator + 1).trim().toLowerCase();
			p.m = monstersbyname.get(name);
			if (p.m == null) {
				inform("[Organization] Unknown monster: " + name);
				continue;
			}
			list.add(p);
			group = group.substring(0, separator);
			try {
				p.min = Integer.parseInt(group);
				p.max = p.min;
			} catch (NumberFormatException e) {
				String[] range = group.split("-");
				p.min = Integer.parseInt(range[0]);
				p.max = Integer.parseInt(range[1]);
			}
		}
		final ArrayList<Encounter> result = new ArrayList<Encounter>();
		if (list.isEmpty()) {
			/* missing monster */
			return result;
		}
		expand(result, list, 0, new ArrayList<Combatant>());
		return result;
	}

	private static void inform(String string) {
		if (Javelin.DEBUG) {
			MonsterReader.log(string);
		}
	}

	private static void expand(final ArrayList<Encounter> result,
			final ArrayList<EncounterPossibilities> possibilites,
			final int depth, final ArrayList<Combatant> monstersp) {
		if (depth == possibilites.size()) {
			final Encounter encounter = new Encounter(monstersp);
			result.add(encounter);
			return;
		}
		final EncounterPossibilities p = possibilites.get(depth);
		for (int i = p.min; i <= p.max; i++) {
			final ArrayList<Combatant> monsters =
					(ArrayList<Combatant>) monstersp.clone();
			for (int j = 0; j < i; j++) {
				monsters.add(new Combatant(null, p.m, true));
			}
			expand(result, possibilites, depth + 1, monsters);
		}
	}

	static public List<Combatant> makeencounter(final int el) {
		List<Encounter> possibilities = new ArrayList<Encounter>();
		for (String terrain : Organization.terrain()) {
			EncounterIndex index = ENCOUNTERS.get(terrain);
			if (index != null) {
				List<Encounter> tier = index.get(el);
				if (tier != null) {
					possibilities.addAll(tier);
				}
			}
		}
		return possibilities.isEmpty() ? null
				: RPG.pick(possibilities).generate();
	}
}
