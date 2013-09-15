package javelin.controller.db.reader;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.encounter.Encounter;
import javelin.controller.encounter.EncounterGenerator;
import javelin.controller.encounter.EncounterPossibilities;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

public class Organization extends FieldReader {
	class EncounterData {
		final ArrayList<String> queue = new ArrayList<String>();
	}

	final static TreeMap<Integer, List<Encounter>> ENCOUNTERS = new TreeMap<Integer, List<Encounter>>();
	private static final TreeMap<Integer, List<Encounter>> AQUATICENCOUNTERS = new TreeMap<Integer, List<Encounter>>();
	static ArrayList<EncounterData> data = new ArrayList<EncounterData>();

	public Organization(final MonsterReader reader, final String fieldname) {
		super(reader, fieldname);
	}

	@Override
	void read(final String value) throws NumberFormatException,
			PropertyVetoException {
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

	static public void process() {
		final HashMap<String, Monster> monstersbyname = new HashMap<String, Monster>();
		addmonsters(monstersbyname, Javelin.ALLMONSTERS);
		addmonsters(monstersbyname, MonsterReader.AQUATICMONSTERS);
		inform("\n== Encounters==");
		for (final EncounterData group : data) {
			for (final Encounter e : prepareexpansion(group.queue,
					monstersbyname)) {
				final TreeMap<Integer, List<Encounter>> table = e.aquatic ? AQUATICENCOUNTERS
						: ENCOUNTERS;
				if (e.size() > EncounterGenerator.MAXGROUPSIZE) {
					continue;
				}
				final int cr = e.rate();
				List<Encounter> tier = table.get(cr);
				if (tier == null) {
					tier = new ArrayList<Encounter>();
					table.put(cr, tier);
				}
				tier.add(e);
			}
		}
		if (Javelin.DEBUG) {
			int total = printtable(ENCOUNTERS);
			total += printtable(AQUATICENCOUNTERS);
			inform("\nTotal encounters: " + total);
		}
		/* Mark for garbage collection */
		data = null;
	}

	public static void addmonsters(
			final HashMap<String, Monster> monstersbyname,
			List<Monster> aLLMONSTERS) {
		for (final Monster m : aLLMONSTERS) {
			monstersbyname.put(m.toString().toLowerCase(), m);
		}
	}

	public static int printtable(TreeMap<Integer, List<Encounter>> table) {
		inform("");
		inform("");
		int i = 0;
		for (int el : table.keySet()) {
			for (Encounter e : table.get(el)) {
				inform("    EL" + el + ": " + e);
				i += 1;
			}
			inform("");
		}
		return i;
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
		expand(result, list, 0, new ArrayList<Monster>());
		return result;
	}

	private static void inform(String string) {
		if (Javelin.DEBUG) {
			MonsterReader.log(string);
		}
	}

	private static void expand(final ArrayList<Encounter> result,
			final ArrayList<EncounterPossibilities> possibilites,
			final int depth, final ArrayList<Monster> monstersp) {
		if (depth == possibilites.size()) {
			final Encounter encounter = new Encounter(monstersp);
			result.add(encounter);
			return;
		}
		final EncounterPossibilities p = possibilites.get(depth);
		for (int i = p.min; i <= p.max; i++) {
			final ArrayList<Monster> monsters = (ArrayList<Monster>) monstersp
					.clone();
			for (int j = 0; j < i; j++) {
				monsters.add(p.m);
			}
			expand(result, possibilites, depth + 1, monsters);
		}
	}

	static public List<Monster> makeencounter(final int el, boolean allowaquatic) {
		List<Encounter> tier = ENCOUNTERS.get(el);
		if (allowaquatic) {
			List<Encounter> aquatic = AQUATICENCOUNTERS.get(el);
			if (aquatic != null) {
				if (tier == null) {
					tier = new ArrayList<Encounter>();
				} else {
					tier = new ArrayList<Encounter>(tier);
				}
				tier.addAll(aquatic);
			}
		}
		return tier == null ? null : RPG.pick(tier).generate();
	}
}
