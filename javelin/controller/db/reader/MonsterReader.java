/**
 * 
 */
package javelin.controller.db.reader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javelin.Javelin;
import javelin.controller.CountingSet;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.SpecialtiesLog;
import javelin.model.unit.BreathWeapon;
import javelin.model.unit.BreathWeapon.BreathArea;
import javelin.model.unit.BreathWeapon.SavingThrow;
import javelin.model.unit.Monster;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MonsterReader extends DefaultHandler {

	public static final ArrayList<Monster> AQUATICMONSTERS = new ArrayList<Monster>();

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
	final ErrorHandler errorhandler = new ErrorHandler();
	private int total;
	final CountingSet debugqualities = new CountingSet();
	final CountingSet sAtks = new CountingSet();
	final CountingSet debugfeats = new CountingSet();

	@Override
	public void startElement(final String uri, final String localName,
			final String name, final Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		// try {
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
			monster.will = getIntAttributeValue(attributes, "Will");
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
			// } else if (localName.equalsIgnoreCase("challengerating")) {
			// monster.srdCr =
			// Float.parseFloat(getNumericalValue(attributes,
			// "Value"));
		} else if (localName.equalsIgnoreCase("avatar")) {
			monster.avatar = attributes.getValue("Name");
			monster.avatarfile = attributes.getValue("Image");
		} else if (localName.equalsIgnoreCase("Organization")) {
			section = "Organization";
		} else if (localName.equalsIgnoreCase("Climateandterrain")) {
			if (attributes.getValue("Terrain").toLowerCase().equals("aquatic")) {
				AQUATICMONSTERS.add(monster);
			}
		} else if (localName.equalsIgnoreCase("Breath")) {
			/* TODO */
			if (attributes.getValue("effect") == null) {
				parsebreath(attributes, monster);
			}
		} else if (localName.equalsIgnoreCase("Spells")) {
			String spellcr = attributes.getValue("cr");
			if (spellcr != null) {
				monster.spellcr = Float.parseFloat(spellcr);
			}
		} else if (monster != null && !localName.equals("StatBlock")) {
			for (final String tagName : new String[] { "description",
					"General", "Terrain", "Treasure", "Alignment",
					"Advancement", "specialabilities", "specialability", "p",
					"combat", "characters", "li", "ul", "CarryingCapacity",
					"ChallengeRating" }) {
				if (localName.equalsIgnoreCase(tagName)) {
					return;
				}
			}
			errorhandler.setInvalid(name);
		}
	}

	static public void parsebreath(final Attributes attributes, final Monster m) {
		final String format = attributes.getValue("format").toLowerCase();
		final String damage = attributes.getValue("damage").toLowerCase();
		final int d = damage.indexOf('d');
		final String save = attributes.getValue("save").toLowerCase();
		final int range = Integer
				.parseInt(format.substring(format.indexOf(' ') + 1));
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
		final int sides = Integer.parseInt(damage.substring(d + 1,
				sign == -1 ? damage.length() : sign));
		final SavingThrow savingthrow = parsebreathsavingthrow(save);
		final int savedc = savingthrow == null ? 0 : Integer.parseInt(save
				.substring(0, save.indexOf(' ')));
		m.breaths.add(new BreathWeapon(attributes.getValue("name")
				.toLowerCase(), format.contains("cone") ? BreathArea.CONE
				: BreathArea.LINE, range, dice, sides, bonus, savingthrow,
				savedc, parsebreatsaveeffect(save), attributes
						.getValue("delay").equals("yes")));
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
		int nMonsters = AQUATICMONSTERS.size();
		for (Entry<Float, List<Monster>> e : Javelin.MONSTERS.entrySet()) {
			final List<Monster> value = e.getValue();
			final int n = value.size();
			nMonsters += n;
			final Float key = e.getKey();
			String listing = "";
			for (final Monster m : value) {
				listing += m.toString() + " (" + m.group + "), ";
				Javelin.ALLMONSTERS.add(m);
			}
			log("CR " + key + " (" + value.size() + "): " + listing);
		}
		log("");
		log(nMonsters + "/" + total + " monsters succesfully loaded.");
	}

	private void debugSpecials(final CountingSet atks, final String string) {
		log("");
		log(string);
		final ArrayList<Entry<String, Integer>> count = new ArrayList<Entry<String, Integer>>(
				atks.getCount());

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

	String cleanArmor(final String speedType) {
		final int beginArmor = speedType.lastIndexOf("(");

		if (beginArmor == -1) {
			return speedType;
		}

		return speedType.substring(0, beginArmor).trim();
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String name) throws SAXException {
		super.endElement(uri, localName, name);
		if (localName.equals("Monster")) {
			if (errorhandler.getInvalid() == null) {
				registermonster();
				// System.out.println(monster + " #name5");
				SpecialtiesLog.log();
			} else {
				errorhandler.informInvalid(this);
				errorhandler.setInvalid(null);
				AQUATICMONSTERS.remove(monster);
			}
			SpecialtiesLog.clear();
		}
		if ("p".equalsIgnoreCase(localName)) {
			describe("\n\n");
		}
		section = null;
	}

	public void registermonster() {
		final float cr;
		try {
			cr = ChallengeRatingCalculator.calculateCr(monster);
		} catch (final Exception e) {
			throw new RuntimeException(
					"Challenge rating issue " + monster.name, e);
		}
		if (AQUATICMONSTERS.contains(monster)) {
			return;
		}
		final List<Monster> mapList = Javelin.MONSTERS.get(cr);
		final List<Monster> addList;
		if (mapList == null) {
			addList = new ArrayList<Monster>();
			Javelin.MONSTERS.put(cr, addList);
		} else {
			addList = mapList;
		}
		addList.add(monster);
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