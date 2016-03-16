/**
 * 
 */
package javelin.controller.db.reader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.reader.factor.ArmorClass;
import javelin.controller.db.reader.factor.Attacks;
import javelin.controller.db.reader.factor.Damage;
import javelin.controller.db.reader.factor.FaceAndReach;
import javelin.controller.db.reader.factor.Feats;
import javelin.controller.db.reader.factor.FieldReader;
import javelin.controller.db.reader.factor.HitDice;
import javelin.controller.db.reader.factor.Initiative;
import javelin.controller.db.reader.factor.Name;
import javelin.controller.db.reader.factor.Organization;
import javelin.controller.db.reader.factor.Paragraph;
import javelin.controller.db.reader.factor.SpecialAttacks;
import javelin.controller.db.reader.factor.SpecialQualities;
import javelin.controller.db.reader.factor.Speed;
import javelin.controller.upgrade.Spell;
import javelin.model.feat.Feat;
import javelin.model.spell.Summon;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;
import javelin.model.unit.abilities.TouchAttack;

/**
 * Reads the monster.xml file at startup.
 * 
 * TODO even though it's fast we could add the monster database to the save file
 * instead of calculating it again. It would of course have to be overridden
 * while {@link Javelin#DEBUG} is on.
 * 
 * @author alex
 */
public class MonsterReader extends DefaultHandler {

	public void describe(String value) {
		if (monster == null) {
			return;
		}
		final String monstername = monster.name;
		String previous = Javelin.DESCRIPTIONS.get(monstername);
		if (previous == null) {
			previous = "";
		}
		Javelin.DESCRIPTIONS.put(monstername, previous + value);
	}

	public Monster monster;
	private String section = null;
	public final ErrorHandler errorhandler = new ErrorHandler();
	private int total;
	public final CountingSet debugqualities = new CountingSet();
	public final CountingSet sAtks = new CountingSet();
	public final CountingSet debugfeats = new CountingSet();
	HashMap<Monster, String> spelldata = new HashMap<Monster, String>();

	@Override
	public void startElement(final String uri, final String localName,
			final String name, final Attributes attributes)
					throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (!Javelin.DEBUG && errorhandler.isinvalid()) {
			return;
		}
		if (localName.equals("Monster")) {
			monster = new Monster();
			total++;
		} else if ("feats".equals(localName.toLowerCase())) {
			section = "Feats";
		} else if (localName.equals("Skills")) {
			section = "Skills";
		} else if (localName.equals("SpecialAttacks")) {
			section = "SpecialAttacks";
		} else if (localName.equals("SpecialQualities")) {
			section = "SpecialQualities";
		} else if (localName.equals("FaceAndReach")) {
			section = "FaceAndReach";
		} else if (localName.equals("Attacks")) {
			section = "Attacks";
		} else if (localName.equals("Damage")) {
			section = "Damage";
		} else if (localName.equals("ArmorClass")) {
			section = "ArmorClass";
		} else if (localName.equals("Name")) {
			section = "Name";
		} else if (localName.equals("Speed")) {
			section = "Speed";
		} else if (localName.equals("Initiative")) {
			section = "Initiative";
		} else if (localName.equals("HitDice")) {
			section = "HitDice";
		} else if (localName.equals("p")) {
			section = "Paragraph";
		} else if (localName.equals("Saves")) {
			monster.fort = getIntAttributeValue(attributes, "Fort");
			monster.ref = getIntAttributeValue(attributes, "Ref");
			monster.setWill(getIntAttributeValue(attributes, "Will"));
		} else if (localName.equals("Abilities")) {
			monster.strength = parseability(attributes, "Str");
			monster.dexterity = parseability(attributes, "Dex");
			monster.constitution = parseability(attributes, "Con");
			monster.intelligence = parseability(attributes, "Int");
			monster.wisdom = parseability(attributes, "Wis");
			monster.charisma = parseability(attributes, "Cha");
		} else if (localName.equals("SizeAndType")) {
			final int size = getSize(attributes.getValue("Size"));

			if (size == -1) {
				errorhandler.setInvalid("Size");
			}

			monster.size = size;

			monster.monsterType = attributes.getValue("Type").toLowerCase();
		} else if (localName.equalsIgnoreCase("avatar")) {
			monster.avatar = attributes.getValue("Name");
			monster.avatarfile = attributes.getValue("Image");
		} else if (localName.equalsIgnoreCase("Organization")) {
			section = "Organization";
		} else if (localName.equalsIgnoreCase("Climateandterrain")) {
			for (String terrain : attributes.getValue("Terrain").toLowerCase()
					.split(",")) {
				// if (terrain == null) {
				// continue; // TODO
				// }
				terrain = terrain.trim();
				if (terrain.isEmpty()) {
					continue;// TODO
				}
				if (terrain.equals("plains") || terrain.equals("hill")
						|| terrain.equals("forest") || terrain.equals("marsh")
						|| terrain.equals("mountains")
						|| terrain.equals("desert")
						|| terrain.equals("underground")
						|| terrain.equals("aquatic")) {
					monster.terrains.add(terrain);
				} else {
					throw new RuntimeException("#unknownterrain " + terrain);
				}
			}
		} else if (localName.equalsIgnoreCase("Breath"))

		{
			/* TODO */
			if (attributes.getValue("effect") == null) {
				parsebreath(attributes, monster);
			}
		} else if (localName.equalsIgnoreCase("Touch"))

		{
			String[] damage = attributes.getValue("damage").split("d");
			monster.touch = new TouchAttack(attributes.getValue("name"),
					Integer.parseInt(damage[0]), Integer.parseInt(damage[1]),
					Integer.parseInt(attributes.getValue("save")));
		} else if (localName.equalsIgnoreCase("Spells"))

		{
			String known = attributes.getValue("known");
			if (known != null) {
				registerspells(known, monster);
				SpecialtiesLog.log("    Spells: " + known);
			}
			String spellcr = attributes.getValue("cr");
			if (spellcr != null) {
				monster.spellcr = Float.parseFloat(spellcr);
				SpecialtiesLog.log("    Spell cr: " + monster.spellcr);
			}
		} else if (monster != null && !localName.equals("StatBlock"))

		{
			for (final String tagName : new String[] { "description", "General",
					"Terrain", "Treasure", "Alignment", "Advancement",
					"specialabilities", "specialability", "p", "combat",
					"characters", "li", "ul", "CarryingCapacity",
					"ChallengeRating" }) {
				if (localName.equalsIgnoreCase(tagName)) {
					return;
				}
			}
			errorhandler.setInvalid(name);
		}

	}

	void registerspells(String known, Monster monster) {
		ArrayList<String> spelllist = new ArrayList<String>();
		HashSet<String> spellset = new HashSet<String>();
		for (String spell : known.split(",")) {
			if (spell.isEmpty()) {
				continue;
			}
			spell = spell.trim().toLowerCase();
			spelllist.add(spell);
			spellset.add(spell);
		}
		for (String spellname : spellset) {
			Spell s = Spell.SPELLS.get(spellname);
			if (s == null) {
				throw new RuntimeException("Uknown spell: " + spellname);
			}
			s = s.clone();
			s.perday = Math.min(5, Collections.frequency(spelllist, spellname));
			monster.spells.add(s);
		}
	}

	static public void parsebreath(final Attributes attributes,
			final Monster m) {
		final String format = attributes.getValue("format").toLowerCase();
		final String damage = attributes.getValue("damage").toLowerCase();
		final int d = damage.indexOf('d');
		final String save = attributes.getValue("save").toLowerCase();
		final int range =
				Integer.parseInt(format.substring(format.indexOf(' ') + 1));
		final int dice = Integer.parseInt(damage.substring(0, d));
		final int bonus;
		final int plus = damage.indexOf('+');
		final int minus = damage.indexOf('-');
		final int sign;
		if (plus >= 0) {
			bonus = Integer.parseInt(damage.substring(plus + 1));
			sign = plus;
		} else if (minus >= 0) {
			bonus = -Integer.parseInt(damage.substring(plus + 1));
			sign = minus;
		} else {
			bonus = 0;
			sign = -1;
		}
		final int sides = Integer.parseInt(
				damage.substring(d + 1, sign == -1 ? damage.length() : sign));
		final SavingThrow savingthrow = parsebreathsavingthrow(save);
		final int savedc = savingthrow == null ? 0
				: Integer.parseInt(save.substring(0, save.indexOf(' ')));
		m.breaths
				.add(new BreathWeapon(attributes.getValue("name").toLowerCase(),
						format.contains("cone") ? BreathArea.CONE
								: BreathArea.LINE,
						range, dice, sides, bonus, savingthrow, savedc,
						parsebreatsaveeffect(save),
						attributes.getValue("delay").equals("yes")));
	}

	public static float parsebreatsaveeffect(final String save) {
		final float saveeffect;
		if (save.contains("half")) {
			saveeffect = .5f;
		} else if (save.contains("negate")) {
			saveeffect = 0f;
		} else {
			saveeffect = 1;
		}
		return saveeffect;
	}

	public static SavingThrow parsebreathsavingthrow(final String save) {
		if (save.contains("ref")) {
			return SavingThrow.REFLEXES;
		}
		if (save.contains("fort")) {
			return SavingThrow.FORTITUDE;
		}
		if (save.contains("will")) {
			return SavingThrow.WILLPOWER;
		}
		return null;
	}

	public static int getSize(final String size) {
		// logger.info("getSize(String " + size + ")");
		int result = -1;
		for (int i = 0; i < Monster.SIZES.length; i++) {
			if (size.compareTo(Monster.SIZES[i]) == 0) {
				result = i;
			}
		}
		return result;
	}

	private int parseability(Attributes attributes, String ability) {
		final int score = getIntAttributeValue(attributes, ability);
		if (score == 0) {
			errorhandler.setInvalid("Unrated attribute: " + ability);
		}
		return score;
	}

	private int getIntAttributeValue(final Attributes attributes,
			final String string) {
		return Integer.parseInt(getNumericalValue(attributes, string));
	}

	private String getNumericalValue(final Attributes attributes,
			final String string) {
		final String value2 = attributes.getValue(string);
		if (value2 == null) {
			log("Monster '" + monster + "' is missing attribute for '" + string
					+ "'");
			return "0";
		}
		return value2.trim();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		debugSpecials(errorhandler.treeError, "Errors:");
		debugSpecials(sAtks, "Special attacks:");
		debugSpecials(debugqualities, "Special qualities:");
		debugSpecials(debugfeats, "Feats:");
		log("");
		postprocessspells();
		int nMonsters = 0;
		for (Monster m : Javelin.ALLMONSTERS) {
			List<Monster> list = Javelin.MONSTERSBYCR.get(m.challengeRating);
			if (list == null) {
				list = new ArrayList<Monster>();
				Javelin.MONSTERSBYCR.put(m.challengeRating, list);
			}
			list.add(m);
		}
		for (Entry<Float, List<Monster>> e : Javelin.MONSTERSBYCR.entrySet()) {
			final List<Monster> value = e.getValue();
			final int n = value.size();
			nMonsters += n;
			final Float key = e.getKey();
			String listing = "";
			for (final Monster m : value) {
				listing += m.toString() + " (" + m.group + "), ";
				// Javelin.ALLMONSTERS.add(m);
			}
			log("CR " + key + " (" + value.size() + "): " + listing);
		}
		log("");
		log(nMonsters + "/" + total + " monsters succesfully loaded.");
	}

	/**
	 * TODO doesn't capture cycle summon references in neither pass, maybe issue
	 * exception?
	 */
	private void postprocessspells() {
		ArrayList<Summon> summonspell = new ArrayList<Summon>();
		ArrayList<Monster> summoncaster = new ArrayList<Monster>();
		ArrayList<Monster> updated = new ArrayList<Monster>();
		for (Monster m : Javelin.ALLMONSTERS) {
			if (m.spells.isEmpty()) {
				continue;
			}
			updated.add(m);
			for (Spell s : m.spells) {
				if (s instanceof Summon) {
					Summon summon = (Summon) s;
					summonspell.add(summon);
					summoncaster.add(m);
				} else {
					s.postloadmonsters();
					m.spellcr += s.cr * s.perday;
				}
			}
		}
		// first pass, self-summon
		for (int i = 0; i < summonspell.size(); i++) {
			Monster m = summoncaster.get(i);
			Summon s = summonspell.get(i);
			if (s.monstername.equalsIgnoreCase(m.name)) {
				s.postloadmonsters();
				m.spellcr += s.cr * s.perday;
				summonspell.set(i, null);
				summoncaster.set(i, null);
			}
		}
		// second pass
		for (Summon s : summonspell) {
			if (s == null) {
				continue;
			}
			s.postloadmonsters();
			summoncaster.get(summonspell.indexOf(s)).spellcr += s.cr * s.perday;
		}
		for (Monster m : updated) {
			ChallengeRatingCalculator.calculateCr(m);
		}
	}

	private void debugSpecials(final CountingSet atks, final String string) {
		log("");
		log(string);
		final ArrayList<Entry<String, Integer>> count =
				new ArrayList<Entry<String, Integer>>(atks.getCount());

		Collections.sort(count, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(final Entry<String, Integer> o1,
					final Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});
		for (final Entry<String, Integer> e : count) {
			log("\t" + e.getKey() + " (" + e.getValue() + ")");
		}
	}

	static int debugnmonsters = 0;

	ArrayList<FieldReader> readers = new ArrayList<FieldReader>();

	{
		readers.add(new Name(this, "Name"));
		// readers.add(new Skills(this, "Skills"));
		readers.add(new Feats(this, "Feats"));
		readers.add(new SpecialQualities(this, "SpecialQualities"));
		readers.add(new FaceAndReach(this, "FaceAndReach"));
		readers.add(new SpecialAttacks(this, "SpecialAttacks"));
		readers.add(new Attacks(this, "Attacks"));
		readers.add(new Damage(this, "Damage"));
		readers.add(new ArmorClass(this, "ArmorClass"));
		readers.add(new Initiative(this, "Initiative"));
		readers.add(new Speed(this, "Speed"));
		readers.add(new HitDice(this, "HitDice"));
		readers.add(new Paragraph(this, "Paragraph"));
		readers.add(new Organization(this, "Organization"));

	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (section == null) {
			return;
		}
		final String value = new String(ch).substring(start, start + length);
		for (final FieldReader r : readers) {
			if (r.fieldname.equals(section)) {
				try {
					r.read(value);
				} catch (final Exception e) {
					throw new RuntimeException("Monster: " + monster.name, e);
				}
			}
		}
	}

	public String cleanArmor(final String speedType) {
		final int beginArmor = speedType.lastIndexOf("(");

		if (beginArmor == -1) {
			return speedType;
		}

		return speedType.substring(0, beginArmor).trim();
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String name) throws SAXException {
		if (localName.equals("Monster")) {
			if (errorhandler.isinvalid()) {
				errorhandler.informInvalid(this);
				errorhandler.setInvalid(null);
			} else {
				for (Feat f : monster.feats) {
					f.update(monster);
				}
				registermonster();
				if (!monster.breaths.isEmpty()) {
					SpecialtiesLog.log("    Breaths: " + monster.breaths);
				}
				SpecialtiesLog.log();
				// Organization.TERRAINDATA.put(monster.toString().toLowerCase(),
				// terrains);
			}
			SpecialtiesLog.clear();
		}
		if ("p".equalsIgnoreCase(localName)) {
			describe("\n\n");
		}
		section = null;
	}

	public void registermonster() {
		if (Javelin.DEBUGALLOWMONSTER != null
				&& !monster.name.contains(Javelin.DEBUGALLOWMONSTER)) {
			return;
		}
		final float cr;
		try {
			cr = ChallengeRatingCalculator.calculateCr(monster);
		} catch (final Exception e) {
			throw new RuntimeException("Challenge rating issue " + monster.name,
					e);
		}
		Javelin.ALLMONSTERS.add(monster);
		// final List<Monster> mapList = Javelin.MONSTERSBYCR.get(cr);
		// final List<Monster> addList;
		// if (mapList == null) {
		// addList = new ArrayList<Monster>();
		// Javelin.MONSTERSBYCR.put(cr, addList);
		// } else {
		// addList = mapList;
		// }
		// addList.add(monster);

	}

	private static PrintWriter log = null;

	public static void log(final String string) {
		if (!Javelin.DEBUG) {
			return;
		}
		if (log == null) {
			try {
				log = new PrintWriter("monsters.log");
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.write(string + "\n");
		log.flush();
	}
}