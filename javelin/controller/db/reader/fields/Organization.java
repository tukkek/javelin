package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterPossibilities;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see FieldReader
 * @see #init()
 */
public class Organization extends FieldReader {
	class EncounterData {
		final ArrayList<String> queue = new ArrayList<String>();
	}

	/** Don't use, temporary. */
	public static HashMap<String, Monster> monstersbyname = new HashMap<String, Monster>(
			Javelin.ALLMONSTERS.size());

	/**
	 * Possible encounters by terrain type.
	 */
	public final static HashMap<String, EncounterIndex> ENCOUNTERSBYTERRAIN = new HashMap<String, EncounterIndex>();
	/**
	 * Possible encounters by monster type (encounters may have more than one
	 * type).
	 */
	public static HashMap<String, List<Encounter>> ENCOUNTERSBYMONSTER = new HashMap<String, List<Encounter>>();
	/**
	 * Random creature expansion (for example: ?mephit expands to any of a
	 * number of mephit types). Hardcoded, initialized by {@link Organization}.
	 */
	public static final HashMap<String, String[]> RANDOM = new HashMap<String, String[]>();
	static final int MAXIMUMSIZE = 12;
	static ArrayList<EncounterData> data = new ArrayList<EncounterData>();

	static {
		RANDOM.put("mephit", new String[] { "Air mephit", "Steam mephit",
				"Dust mephit", "Earth mephit", "Fire mephit", "Magma mephit",
				"Ice mephit", "Ooze mephit", "Salt mephit", "Water mephit" });
	}

	/** See {@link FieldReader#FieldReader(MonsterReader, String)}. */
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

	/**
	 * Uses organization data to create {@link Encounter}s.
	 */
	static public void init() {
		for (Monster m : Javelin.ALLMONSTERS) {
			monstersbyname.put(m.toString().toLowerCase(), m);
		}
		inform("\n== Encounters==");
		for (final EncounterData group : data) {
			for (final Encounter e : prepareexpansion(group.queue,
					monstersbyname)) {
				register(e);
			}
		}
		new NpcGenerator().generate();
		if (Javelin.DEBUG) {
			inform("");
			inform("");
			int i = 0;
			for (String terrain : ENCOUNTERSBYTERRAIN.keySet()) {
				for (int el : ENCOUNTERSBYTERRAIN.get(terrain).keySet()) {
					for (Encounter e : ENCOUNTERSBYTERRAIN.get(terrain)
							.get(el)) {
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
		monstersbyname = null;
	}

	static void register(final Encounter e) {
		if (e.group.size() > MAXIMUMSIZE) {
			return;
		}
		final HashSet<String> terrains = new HashSet<String>();
		jointerrains(e.group, terrains);
		for (final String terrain : terrains) {
			EncounterIndex index = ENCOUNTERSBYTERRAIN.get(terrain);
			if (index == null) {
				index = new EncounterIndex();
				ENCOUNTERSBYTERRAIN.put(terrain, index);
			}
			index.put(e.rate(), e);
		}
		HashSet<String> monsters = new HashSet<String>();
		for (Combatant c : e.group) {
			String monster = c.source.name;
			if (monsters.add(monster)) {
				List<Encounter> encounters = ENCOUNTERSBYMONSTER.get(monster);
				if (encounters == null) {
					encounters = new ArrayList<Encounter>();
					ENCOUNTERSBYMONSTER.put(monster, encounters);
				}
				encounters.add(e);
			}
		}
	}

	static void jointerrains(List<Combatant> group,
			final HashSet<String> terrains) {
		for (final Combatant c : group) {
			for (final String terrain : c.source.getterrains()) {
				terrains.add(terrain);
			}
		}
	}

	private static List<Encounter> prepareexpansion(ArrayList<String> queue,
			HashMap<String, Monster> monstersbyname) {
		final ArrayList<EncounterPossibilities> list = new ArrayList<EncounterPossibilities>();
		for (String group : queue) {
			final EncounterPossibilities p = new EncounterPossibilities();
			group = group.trim();
			int separator = group.indexOf(' ');
			final String name = group.substring(separator + 1).trim()
					.toLowerCase();
			if (name.contains("?")) {
				p.random = name.replace("?", "");
			} else {
				p.m = monstersbyname.get(name);
				if (p.m == null) {
					inform("[Organization] Unknown monster: " + name);
					continue;
				}
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

	static void inform(String string) {
		if (Javelin.DEBUG) {
			MonsterReader.log(string, "organization.log");
		}
	}

	static void expand(final ArrayList<Encounter> result,
			final ArrayList<EncounterPossibilities> possibilites,
			final int depth, final ArrayList<Combatant> monstersp) {
		if (depth == possibilites.size()) {
			final Encounter encounter = new Encounter(monstersp);
			result.add(encounter);
			return;
		}
		final EncounterPossibilities p = possibilites.get(depth);
		for (int i = p.min; i <= p.max; i++) {
			final ArrayList<Combatant> monsters = (ArrayList<Combatant>) monstersp
					.clone();
			for (int j = 0; j < i; j++) {
				monsters.add(new Combatant(p.getmonster(), true));
			}
			expand(result, possibilites, depth + 1, monsters);
		}
	}
}
