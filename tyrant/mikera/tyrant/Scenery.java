// Root class for scenic items and decorations on maps

package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Scenery {

	public static void init() {
		Thing t;
		t = Lib.extendCopy("base scenery", "base thing");
		t.set("Frequency", 30);
		t.set("IsScenery", 1);
		t.set("IsPhysical", 1);
		t.set("IsDestructible", 1);
		t.set("IsJumpable", 1);
		t.set("ImageSource", "Scenery");
		t.set("IsBlocking", 1);
		t.set("IsOwned", 1);
		t.set("MapColour", 0x00908070);
		t.set("Z", Thing.Z_ITEM - 1);
		t.set("ASCII", "#");
		t.set("HPS", 10);
		Lib.add(t);

		t = Lib.extend("sewer", "base scenery");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.set("OnAction", Scripts.generator("sewer rat", 10));
		t.set("LevelMin", 4);
		t.set("LevelMin", 3);
		t.set("IsBlocking", 0);
		t.set("Image", 303);
		Lib.add(t);

		t = Lib.extend("rat cave", "base scenery");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.set("IsBlocking", 0);
		t.set("OnAction", Scripts.generator("[IsRat]", 20));
		t.set("LevelMin", 2);
		t.set("Image", 303);
		Lib.add(t);

		t = Lib.extend("vortex", "base scenery");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.addHandler("OnAction", Scripts.generator(null, 500));
		t.set("LevelMin", 3);
		t.set("Image", 40);
		Lib.add(t);

		t = Lib.extend("demon vortex", "base scenery");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.addHandler("OnAction", Scripts.generator("[IsDemonic]", 500, 29));
		t.set("LevelMin", 31);
		t.set("Image", 40);
		Lib.add(t);

		t = Lib.extend("greater demon vortex", "base scenery");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.addHandler("OnAction", Scripts.generator("[IsDemonic]", 500, 36));
		t.set("LevelMin", 39);
		t.set("Image", 40);
		Lib.add(t);

		Fire.init();
		Portal.init();

		initTrees();
		initFruitTrees();
		initBushes();
		initSigns();
		initPlants();
		initFurniture();
		initGraveStones();
		initWells();
		initWebs();
		initMasonry();
		initFountains();
		initFortifications();
		initAltars();
	}

	private static void initMasonry() {
		Thing t;

		t = Lib.extend("base masonry", "base scenery");
		t.set("Material", "stone");
		t.set("RES:impact", -10);
		Lib.add(t);

		t = Lib.extend("stone bench", "base masonry");
		t.set("Image", 225);
		t.set("LevelMin", 10);
		t.set("HPS", 85);
		t.set("IsBlocking", 0);
		Lib.add(t);

		t = Lib.extend("menhir", "base masonry");
		t.set("Image", 228);
		t.set("LevelMin", 1);
		t.set("HPS", 285);
		t.set("IsBlocking", 0);
		Lib.add(t);

		t = Lib.extend("bird bath", "base masonry");
		t.set("Image", 224);
		t.set("LevelMin", 7);
		t.set("HPS", 60);
		t.set("IsBlocking", 1);
		Lib.add(t);
	}

	private static void initFurniture() {
		Thing t = Lib.extend("base furniture", "base scenery");
		t.set("IsFurniture", 1);
		t.set("Image", 200);
		t.set("HPS", 30);
		t.set("IsBlocking", 1);
		t.set("IsMonster", 1);
		t.set("IsHostile", 1);
		t.set("IsLiving", 1);
		Lib.add(t);

		t = Lib.extend("table", "base furniture");
		t.set("UName", "table");
		t.set("Image", 200);
		t.set("LevelMin", 7);
		t.set("Z", Thing.Z_ONFLOOR - 1);
		Lib.add(t);

		t = Lib.extend("NS table", "table");
		t.set("LevelMin", 7);
		t.set("Image", 202);
		Lib.add(t);

		t = Lib.extend("EW table", "table");
		t.set("LevelMin", 7);
		t.set("Image", 201);
		Lib.add(t);

		t = Lib.extend("stool", "base furniture");
		t.set("Image", 203);
		t.set("HPS", 15);
		t.set("IsBlocking", 1);
		t.set("LevelMin", 6);
		Lib.add(t);

		t = Lib.extend("tent", "base furniture");
		t.set("Image", 304);
		t.set("LevelMin", 8);
		t.set("IsBlocking", 1);
		Lib.add(t);

	}

	private static void initWebs() {
		Thing t = Lib.extend("base web", "base scenery");
		t.set("IsWeb", 1);
		t.set("NoStack", 1);
		t.set("Image", 48);
		t.set("HPS", 15);
		t.set("IsBlocking", 0);
		t.set("IsWarning", 1);
		t.set("RES:impact", 10);
		t.set("RES:piercing", 20);
		t.set("RES:fire", -13);
		t.set("LevelMin", 1);
		{
			Script s = Scripts.addThing("Target", "web");
			s = Scripts.combine(s, Scripts.die());
			t.addHandler("OnEnterTrigger", s);
		}
		Lib.add(t);

		t = Lib.extend("spider web", "base web");
		t.set("IsActive", 1);
		t.set("DecayRate", 20);
		Lib.add(t);

		t = Lib.extend("giant spider web", "base web");
		t.set("HPS", 40);
		Lib.add(t);

		t = Lib.extend("spider infested web", "base web");
		t.set("IsActive", 1);
		t.set("IsGenerator", 1);
		t.addHandler("OnAction", Scripts.generator("baby giant spider", 30));

		t.set("LevelMin", 14);
		t.set("HPS", 150);
		Lib.add(t);

	}

	private static void initSigns() {
		Thing t = Lib.extend("blank sign", "base scenery");
		t.set("IsSign", 1);
		t.set("Image", 64);
		t.set("HPS", 20);
		t.set("IsBlocking", 0);
		t.set("LevelMin", 1);
		Lib.add(t);

		t = Lib.extend("bank sign", "blank sign");
		t.set("Image", 67);
		Lib.add(t);

		t = Lib.extend("armoury sign", "blank sign");
		t.set("Image", 63);
		Lib.add(t);

		t = Lib.extend("store sign", "blank sign");
		t.set("Image", 60);
		Lib.add(t);

		t = Lib.extend("magic shop sign", "blank sign");
		t.set("Image", 62);
		Lib.add(t);

		t = Lib.extend("smithy sign", "blank sign");
		t.set("Image", 61);
		Lib.add(t);

		t = Lib.extend("food shop sign", "blank sign");
		t.set("Image", 60);
		Lib.add(t);

		t = Lib.extend("inn sign", "blank sign");
		t.set("Image", 61);
		Lib.add(t);
	}

	private static void initPlants() {
		Thing t;
		t = Lib.extend("base plant", "base scenery");
		t.set("IsPlant", 1);
		t.set("Image", 81);
		t.set("HPS", 5);
		t.set("IsViewBlocking", 0);
		t.set("IsBlocking", 0);
		Lib.add(t);

		t = Lib.extend("plant", "base plant");
		t.set("LevelMin", 1);
		Lib.add(t);

		t = Lib.extend("sweet-smelling plant", "plant");
		t.set("LevelMin", 10);
		{
			final Modifier m = Modifier.bonus(Skill.HEALING, 2);
			m.set("ApplyMessage", "You smell the sweet scent of the flower");
			t.add("LocationModifiers", m);
		}
		Lib.add(t);

		t = Lib.extend("potted plant", "base plant");
		t.set("Image", 226);
		t.set("LevelMin", 1);
		Lib.add(t);

		t = Lib.extend("potted flower", "base plant");
		t.set("Image", 227);
		t.set("LevelMin", 1);
		Lib.add(t);
	}

	private static class WellScript extends Script {
		private static final long serialVersionUID = 3832620690351338806L;

		@Override
		public boolean handle(final Thing t, final Event e) {
			final Thing tt = e.getThing("Target");
			if (!tt.isHero()) {
				return false;
			}

			Thing gift = Game.selectItem("Select an item to drop in the well:",
					tt);
			if (gift == null) {
				Game.messageTyrant("");
				return false;
			}

			gift = gift.remove(1);
			Game.messageTyrant("You drop " + gift.getTheName() + " into "
					+ t.getTheName());
			Game.messageTyrant("Splooosh!");

			// need coin or valuable item
			int value = Item.value(gift) > 10000 ? 1 : 0;
			if (gift.getFlag("IsCoin")) {
				value = 1;
			}

			if (value > 0 && RPG.r(100) < t.getStat("WishChance")) {
				Wish.doWish();
			}

			// no more wish chances!
			t.set("WishChance", 0);
			return true;
		}
	}

	private static class AltarScript extends Script {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean handle(final Thing t, final Event e) {
			final Thing h = e.getThing("Target");
			if (!h.isHero()) {
				return false;
			}

			e.set("ActionTaken", 1);

			final String heroReligion = h.getString("Religion");
			final String altarReligion = t.getString("Religion");

			if (altarReligion != null && !altarReligion.equals(heroReligion)) {
				h.message("This altar is dedicated to " + altarReligion);
				h.message("Since you worship " + heroReligion
						+ ", you may not make sacrifices here.");
			}

			while (true) {
				final Thing sac = Game.selectItem(
						"Select an item to scrifice to " + heroReligion,
						h.getItems());
				if (sac == null) {
					// no more sacrifices
					return false;
				}

				Gods.sacrifice(h, sac);
			}
		}
	}

	private static class FountainScript extends Script {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean handle(final Thing t, final Event e) {
			final Thing h = e.getThing("Target");
			if (!h.isHero()) {
				return false;
			}

			e.set("ActionTaken", 1);
			h.message("You drink from the fountain");
			h.incStat("APS", -100);
			Being.heal(h, 1);

			if (RPG.d(30) == 1) {
				h.message(t.getTheName() + " dries up");
				t.replaceWith("dry fountain");
			}

			return false;
		}
	}

	private static void initFountains() {
		Thing t;

		t = Lib.extend("base fountain", "base scenery");
		t.set("HPS", 200);
		t.set("IsFountain", 1);
		t.set("IsBlocking", 1);
		t.set("Frequency", 30);
		t.set("LevelMin", 1);
		t.set("Image", 249);
		t.set("ASCII", "0");
		Lib.add(t);

		t = Lib.extend("fountain", "base fountain");
		t.set("OnBump", new FountainScript());
		Lib.add(t);

		t = Lib.extend("dry fountain", "base fountain");
		t.set("Image", 250);
		Lib.add(t);

	}

	private static void initFortifications() {
		Thing t;

		t = Lib.extend("base fortification", "base scenery");
		t.set("HPS", 100);
		t.set("IsFortification", 1);
		t.set("IsBlocking", 1);
		t.set("Frequency", 30);
		t.set("LevelMin", 1);
		t.set("Image", 70);
		t.set("ASCII", "x");
		Lib.add(t);

		t = Lib.extend("barricade", "base fortification");
		Lib.add(t);
	}

	private static void initAltars() {
		Thing t;
		t = Lib.extend("base altar", "base scenery");
		t.set("HPS", 200);
		t.set("IsAltar", 1);
		t.set("IsBlocking", 1);
		t.set("Frequency", 30);
		t.set("LevelMin", 1);
		t.set("Image", 240);
		t.set("OnBump", new AltarScript());
		t.set("ASCII", "_");
		Lib.add(t);

		t = Lib.extend("golden altar", "base altar");
		Lib.add(t);

		t = Lib.extend("chaos altar", "base altar");
		t.set("Image", 246);
		Lib.add(t);

		t = Lib.extend("stone altar", "base altar");
		t.set("Image", 245);
		Lib.add(t);

		t = Lib.extend("dark altar", "base altar");
		t.set("Image", 247);
		Lib.add(t);
	}

	private static void initWells() {
		Thing t;
		t = Lib.extend("base well", "base scenery");
		t.set("HPS", 200);
		t.set("IsWell", 1);
		t.set("IsBlocking", 1);
		t.set("Frequency", 100);
		t.set("LevelMin", 1);
		t.set("OnBump", new WellScript());
		t.set("Image", 223);
		Lib.add(t);

		t = Lib.extend("well", "base well");
		Lib.add(t);

		t = Lib.extend("magic well", "base well");
		t.set("Frequency", 10);
		t.set("Image", 222);
		t.set("WishChance", 2);
		t.set("DeathDecoration", "[IsMagicItem]");
		Lib.add(t);

		t = Lib.extend("wishing well", "base well");
		t.set("Frequency", 1);
		t.set("WishChance", 100);
		t.set("IsActive", 1);
		t.addHandler("OnAction", Scripts.generator("flutterby", 2000));
		t.set("DeathDecoration", "Ultimate Destruction");
		Lib.add(t);

	}

	private static class GraveStoneScript extends Script {
		private static final long serialVersionUID = -2908356113863537197L;
		private static String[] inscriptions = new String[] {
				"Justin Tyme",
				"Yetta Nuther",
				"Barry D'Alyve",
				"Dawn Under",
				"Ted N. Burried",
				"Yul B. Nechzt",
				"Bill M. Layder",
				"Leff B. Hynde",
				"Kerry M. Off",
				"Fester N. Rott",
				"Reid N. Weep",
				"Sue D'Bum",
				"Jess Gough",
				"Barry M. Deep",
				"U. R. Gonne",
				"Davy Jones",
				"Otta B. Alyve",
				"B. Ware",
				"Dr. U. Dysoon",
				"Berry D. Hatchet",
				"R. U. Next",
				"Dr. Izzy Gone",
				"C3PO - Rust in Peace",
				"R.I.P",
				"Here Lies Mozart Decomposing",
				"As you pass by\nAnd cast an eye\nAs you are now\nSo once was I",
				"Here lies the body of my sweet sister\nShe was just fine 'til Dracula kissed her",
				"Treasure Hunter Lies Below\nA Ghastly Curse Laid Him Low",
				"If money thou art in need of any, dig three feet and find a penny!",
				"Treasure Seeker Dug Deep One Night\nTo His Misfortune, He died Of Fright." };

		@Override
		public boolean handle(final Thing t, final Event e) {
			final int c = t.x + t.y * 103 + 10017;
			final Thing h = e.getThing("Target");
			if (h != Game.hero()) {
				return false;
			}
			if (h.getFlag(Skill.LITERACY)) {
				Game.messageTyrant("You read the inscription on "
						+ t.getTheName() + ":");
				final String ins = inscriptions[c % inscriptions.length];
				final String[] ss = ins.split("\n");
				for (final String element : ss) {
					Game.messageTyrant("  \"" + element + "\"");
				}
			} else {
				Game.messageTyrant("You notice some inscriptions on "
						+ t.getTheName());
			}
			return false;
		}
	}

	private static void initGraveStones() {
		Thing t;
		t = Lib.extend("base gravestone", "base scenery");
		t.set("HPS", 50);
		t.set("IsGravestone", 1);
		t.set("LevelMin", 1);
		t.set("IsBlocking", 1);
		t.set("DeathDecoration", "ghost");
		t.set("OnBump", new GraveStoneScript());
		t.set("Image", 220);
		Lib.add(t);

		t = Lib.extend("gravestone", "base gravestone");
		Lib.add(t);

		t = Lib.extend("small gravestone", "base gravestone");
		t.set("HPS", 30);
		t.set("IsBlocking", 1);
		t.set("DeathDecoration", "[IsUndead]");
		Lib.add(t);

	}

	// Unfinished sawing action. Commented out for checkin
	// But left the code in place for feedback if you read it. -Rick
	/*
	 * private static boolean isSawable(Thing thing) { return
	 * thing.getFlag("IsSawable"); }
	 * 
	 * public static int sawAbility() { return 100; }
	 * 
	 * public static int sawCost(Thing who, Thing what) { int sawing =
	 * who.getStat(Skill.WOODWORK); if (who.getFlag("sawing")) { return 100; }
	 * if (sawing <= 0) { return 0; } if ((what != null) &&
	 * what.getFlag("IsSawingTool")) { return what.getStat("SawCost") / sawing;
	 * } return 0; }
	 * 
	 * public static boolean saw(Thing who, Map map, int x, int y) { Thing tree
	 * = map.getThings(x,y)[0]; Thing what = who.getWielded(RPG.WT_MAINHAND);
	 * 
	 * if (isSawable(tree)) { int cost = sawCost(who,what); if (cost <= 0) {
	 * return false; } int hard = tree.getStat("SawDifficulty"); if (RPG.r(hard)
	 * < sawAbility()) { if (what != null) {
	 * what.damage(what.getStat("SawDamage"),RPG.DT_SPECIAL); }
	 * who.incStat("APS", -cost); who.message("You saw down the " +
	 * tree.name()); return Scenery.saw(tree); }
	 * who.message("You saw furiously at the " + tree.name()); return false; }
	 * who.message("You are unable to saw through " + tree.getTheName()); return
	 * false; }
	 * 
	 * public static boolean saw(Thing tree) { return true; }
	 */

	private static void initTrees() {
		Thing t;
		t = Lib.extend("base tree", "base scenery");
		t.set("IsJumpable", 0);
		t.set("IsBaseTree", 1);
		t.set("IsTree", 1);
		t.set("Image", 83);
		t.set("HPS", 100);
		t.set("ARM", 4);
		t.set("IsViewBlocking", 1);
		t.set("IsBlocking", 1);
		t.set("DeathDecoration", "wooden log");
		Lib.add(t);

		t = Lib.extend("tree", "base tree");
		t.set("LevelMin", 1);
		t.set("IsSawable", 1);
		t.set("SawDifficulty", 30);
		t.set("IsChopable", 1);
		t.set("Image", 90);
		Lib.add(t);

		t = Lib.extend("pine tree", "base tree");
		t.set("LevelMin", 1);
		t.set("IsSawable", 1);
		t.set("SawDifficulty", 30);
		t.set("IsChopable", 1);
		t.set("Image", 94);
		Lib.add(t);

		t = Lib.extend("small tree", "tree");
		t.set("IsViewBlocking", 0);
		t.set("Image", 90);
		Lib.add(t);

		t = Lib.extend("tree stump", "tree");
		t.set("Z", Thing.Z_ONFLOOR + 1);
		t.set("IsViewBlocking", 0);
		t.set("IsBlocking", 0);
		t.set("IsOwned", 0);
		t.set("Image", 84);
		Lib.add(t);

		t = Lib.extend("withered tree", "tree");
		t.set("Image", 83);
		t.set("IsViewBlocking", 0);
		Lib.add(t);

		t = Lib.extend("large tree", "tree");
		t.set("Image", 82);
		t.set("HPS", 300);
		Lib.add(t);
	}

	/**
	 * Fruit trees are generators for particular types of fruit useful source of
	 * food and money at low levels
	 * 
	 * Note: the LevelMin of the fruit tree should be roughly in line with the
	 * LevelMin of the fuit - otherwise you will get lots of ludicrously
	 * expensive fruit at low levels
	 * 
	 */
	private static void initFruitTrees() {
		Thing t;

		t = Lib.extend("base fruit tree", "tree");
		t.set("Image", 91);
		t.set("IsFruitTree", 1);
		t.set("IsActive", 1);
		Lib.add(t);

		t = Lib.extend("apple tree", "base fruit tree");
		t.set("ViewBlocking", 0); // can see under
		t.addHandler("OnAction", Scripts.generator("apple,cooking apple", 10));
		Lib.add(t);

		t = Lib.extend("red apple tree", "apple tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("red apple,juicy apple", 10));
		Lib.add(t);

		t = Lib.extend("crab apple tree", "apple tree");
		t.set("Image", 91);
		t.set("IsFruitTree", 0);
		t.addHandler("OnAction", Scripts.generator("crab apple", 15));
		Lib.add(t);

		t = Lib.extend("cherry tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("cherry", 15));
		t.set("LevelMin", 7);
		Lib.add(t);

		t = Lib.extend("ranier cherry tree", "base fruit tree");
		t.set("Image", 91);
		t.set("Frequency", 5);
		t.addHandler("OnAction", Scripts.generator("ranier cherry", 10));
		t.set("LevelMin", 17);
		Lib.add(t);

		t = Lib.extend("peach tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("peach", 15));
		t.set("LevelMin", 9);
		Lib.add(t);

		t = Lib.extend("orange tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("orange", 15));
		t.set("LevelMin", 10);
		Lib.add(t);

		t = Lib.extend("lemon tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("lemon", 15));
		t.set("LevelMin", 7);
		Lib.add(t);

		t = Lib.extend("plum tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("plum", 15));
		t.set("LevelMin", 4);
		Lib.add(t);

		t = Lib.extend("apricot tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("apricot", 15));
		t.set("LevelMin", 12);
		Lib.add(t);

		t = Lib.extend("lime tree", "base fruit tree");
		t.set("Image", 91);
		t.addHandler("OnAction", Scripts.generator("lime", 15));
		t.set("LevelMin", 10);
		Lib.add(t);

		t = Lib.extend("key lime tree", "base fruit tree");
		t.set("Image", 91);
		t.set("Frequency", 5);
		t.addHandler("OnAction", Scripts.generator("key lime", 10));
		t.set("LevelMin", 20);
		Lib.add(t);
	}

	private static void initBushes() {
		Thing t;
		t = Lib.extend("bush", "base tree");
		t.set("IsViewBlocking", 1);
		t.set("IsBlocking", 0);
		t.set("IsBush", 1);
		t.set("HPS", 10);
		t.set("ARM", 2);
		t.set("LevelMin", 1);
		t.set("Image", 92);
		t.set("DeathDecoration", "stick");
		Lib.add(t);

		t = Lib.extend("small bush", "bush");
		t.set("IsViewBlocking", 0);
		t.set("IsBlocking", 0);
		t.set("Image", 92);
		Lib.add(t);

		t = Lib.extend("thick bush", "bush");
		t.set("IsViewBlocking", 1);
		t.set("IsBlocking", 1);
		t.set("HPS", 30);
		t.set("Image", 80);
		Lib.add(t);

		t = Lib.extend("thorny bush", "bush");
		t.set("IsViewBlocking", 0);
		t.set("IsBlocking", 1);
		// TODO: allow crawling through via OnBump
		// TODO: consequent damage
		t.set("Image", 83);
		Lib.add(t);

		t = Lib.extend("base berry patch", "bush");
		t.set("IsViewBlocking", 0);
		t.set("IsBlocking", 0);
		t.set("IsBerryBush", 1);
		t.set("IsFruit", 1);
		t.set("IsActive", 1);
		t.set("Image", 92);
		Lib.add(t);

		t = Lib.extend("strawberry patch", "base berry patch");
		t.set("Image", 92);
		t.addHandler("OnAction", Scripts.generatorInPlace("strawberry", 15));
		Lib.add(t);

		t = Lib.extend("raspberry patch", "base berry patch");
		t.set("Image", 92);
		t.addHandler("OnAction", Scripts.generatorInPlace("raspberry", 15));
		Lib.add(t);

		t = Lib.extend("blueberry bush", "base berry patch");
		t.set("Image", 92);
		t.addHandler("OnAction", Scripts.generatorInPlace("blueberry", 15));
		Lib.add(t);

		t = Lib.extend("grape vine", "base berry patch");
		t.set("Image", 92);
		t.addHandler("OnAction", Scripts.generatorInPlace("grape", 15));
		Lib.add(t);
	}
}