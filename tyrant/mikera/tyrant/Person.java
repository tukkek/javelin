package tyrant.mikera.tyrant;

import java.util.ArrayList;
import java.util.Arrays;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;

/**
 * @author Mike
 * 
 *         This class contains frienly "Characters" used to populate towns ,
 *         etc.
 */
public class Person {

	public static void init() {
		Thing t = Lib.extend("base person", "base being");
		t.set("LevelMin", 1);
		t.set("IsPerson", 1);
		t.set("IsHostile", 0);
		t.set("IsIntelligent", 1);
		t.set("IsInhabitant", 1);
		t.set("IsGiftReceiver", 1);
		t.set("Frequency", 0);
		Lib.add(t);

		initRaces();
		initTownies();
		initWoodFolk();
		initNPCs();
		initWanderers();
		initUniversityStaff();
		initDwarves();
	}

	private static Script teachSkillScript(String skill, int price) {
		Script s = new TeachSkillScript();
		s.set("SkillName", skill);
		s.set("Price", price);
		return s;
	}

	// private static Script teachSkillScript(String skill, int price , String
	// practitioner ) {
	// Script s=new TeachSkillScript();
	// s.set("SkillName",skill);
	// s.set("Price",price);
	// s.set("Practitioner" , practitioner);
	// return s;
	// }

	private static class TeachSkillScript extends Script {
		private static final long serialVersionUID = 6064405547665614436L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing h = e.getThing("Target");

			String sk = getString("SkillName");
			int price = getStat("Price");

			if (h.getStat(sk) > 0) {
				if (get("Practitioner") != null) {
					Game.messageTyrant("\"Good day to you, fellow "
							+ get("Practitioner") + ".\"");
				} else {
					Game.messageTyrant("\"Good day to you!\"");
				}
			} else {
				if (h.getStat(RPG.ST_SKILLPOINTS) >= 2) {
					if (Coin.getMoney(h) < price) {
						Game.messageTyrant(t.getTheName()
								+ " could teach you the " + sk + " skill for "
								+ Coin.valueString(price) + "");
						Game.messageTyrant(
								"Unfortunately, you do not have enough money to pay for this");
					} else {
						// can learn skill
						Game.messageTyrant(t.getTheName()
								+ " is willing to help you acquire new knowledge.");
						Game.messageTyrant("Would you like to learn the " + sk
								+ " skill for " + Coin.valueString(price)
								+ "? (y/n)");
						char c = Game.getOption("yn");
						if (c == 'y') {
							h.incStat(sk, 1);
							h.incStat(RPG.ST_SKILLPOINTS, -2);
							Coin.removeMoney(h, price);
							Game.messageTyrant("You learn the " + sk
									+ " skill from " + t.getTheName());
						} else {
							Game.messageTyrant("Good day to you then!");
						}
					}
				} else {
					// not enough experience
					Game.messageTyrant(t.getTheName() + " can teach the " + sk
							+ " skill for " + Coin.valueString(price) + "");
					Game.messageTyrant(
							"However, you must gain more experience first");
				}
			}

			return false;
		}
	}

	private static class SwapScript extends Script {
		private static final long serialVersionUID = -1462156804224496122L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing gift = e.getThing("Gift");
			Thing giver = e.getThing("Giver");

			if (getStat("SwapCount") <= 0) {
				giver.message(t.getTheName() + " has nothing else to trade");
				return false;
			}

			if (!gift.getFlag(getString("WantedFlag"))) {
				giver.message(
						t.getTheName() + " doesn't want to swap anything for "
								+ gift.getYourName());
				return false;
			}
			int value = Item.value(gift);

			for (int i = 0; i < 20; i++) {
				Thing st = Lib.createType(getString("SwapFlag"),
						getStat("SwapLevel"));

				if (st != null && Item.value(st) < value) {
					if (getFlag("IdentifyFlag")) {
						Item.identify(st);
					}
					giver.message(t.getTheName() + " gives you " + st.getAName()
							+ " in return");
					gift.remove();
					giver.addThingWithStacking(st);
					incStat("SwapCount", -1);
					return true;
				}
			}

			giver.message(t.getTheName() + " hasn't got anything to swap for "
					+ gift.getYourName());

			return false;
		}

		public static SwapScript create(String wanted, String swap, int level) {
			SwapScript ss = new SwapScript();
			ss.set("WantedFlag", wanted);
			ss.set("SwapFlag", swap);
			ss.set("SwapLevel", level);
			ss.set("SwapCount", RPG.d(6));
			ss.set("IdentifyFlag", 1);
			return ss;
		}
	}

	private static class ListSellingScript extends Script {
		private static final long serialVersionUID = 5075582255481317201L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing h = e.getThing("Target");

			for (Thing gift =
					Game.selectSaleItem("Select an item to sell:", h, t); // Declaration
																			// part
																			// of
																			// for
																			// loop
			gift != null; // Stop once gift is null
			gift = Game.selectSaleItem("Select an item to sell:", h, t)) { // Updater
																			// :
																			// almost
																			// same
																			// as
																			// declaration
																			// part
				if (gift == null) {
					return true;
				} // Sanity check for elusive bug
				if (gift.getFlag("IsMoney")) {
					continue;
				}

				int total = gift.getStat("Number");
				int count = 1;
				if (total > 1) {
					// count =
					// Game.getNumber("Sell how many (Enter=all)?",total);
					count = Game.selectSaleNumber("Sell how many (Enter=All)? ",
							h, t, total);
				}
				int value = Item.shopValue(gift, h, t, count);

				if (gift.y > 0 && !h.clearUsage(gift.y)) {
					continue;
				}

				Game.messageTyrant(
						"You sell your " + gift.getName(Game.hero(), count)
								+ " to " + t.getTheName());
				if (count > 0) {
					gift.unequip(count);
				}
				Coin.addMoney(h, value);
			}

			return true;
		}

	}

	private static class SellingScript extends Script {
		private static final long serialVersionUID = -6816925315754576522L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing gift = e.getThing("Gift");
			Thing giver = e.getThing("Giver");

			if (gift != null) {
				int value = Item.shopValue(gift, giver, t);

				value = Coin.roundMoney(value);

				if (value > 0) {
					giver.message(t.getTheName() + " offers you "
							+ Coin.valueString(value) + " for your "
							+ gift.getName(Game.hero()));
					giver.message("Do you accept? (y/n)");
					char k = Game.getOption("ynq");
					if (k == 'y') {
						Game.messageTyrant(
								"You sell your " + gift.getName(Game.hero())
										+ " to " + t.getTheName());
						gift.remove();
						Coin.addMoney(giver, value);
					}
				} else {
					giver.message(t.getTheName() + " doesn't want to buy your "
							+ gift.getName(Game.hero()));
				}

				return true;
			}
			throw new Error("Giving null gift!");
		}

	}

	private static class RepairScript extends Script {

		/*
		 * @author ppirrip 13, Aug, 04 TODO Make the smith repair damaged items
		 * with a price. Based on the swap script should be sufficient. NOTE
		 * Might not work with multiple items!!!
		 */

		private static final long serialVersionUID = -6588466277758378986L;

		@Override
		public boolean handle(Thing t, Event e) {
			// first try repair for free.
			Thing gift = e.getThing("Gift");
			Thing giver = e.getThing("Giver");
			// maybe I should not trust the isDamaged()
			// boolean damagedFlag = gift.isDamaged();
			boolean damagedFlag =
					gift.getStat("HPS") < gift.getStat("HPSMAX") ? true : false;
			/*
			 * // Debugging System.out.println("Current HPS = "+
			 * gift.getStat("HPS")); System.out.println("Max HPS = "+
			 * gift.getStat("HPSMAX")); if (damagedFlag) { System.out.println(
			 * "True [ppirrip]"); } else { System.out.println("False [ppirrip]"
			 * ); }
			 */
			// the item to be fixed has to be damage-able
			if (gift != null && damagedFlag) {
				// TODO: chk is the gift damaged or not

				int value = Item.value(gift); // value of the thing
				int hskill = giver.getStat("CH") * giver.getStat(Skill.TRADING);
				int sskill = t.getStat("CH");
				// for debugging
				value = (int) (0.3 * (value * 0.3
						+ 0.7 * value * (hskill / (hskill + sskill))));

				if (value > 0) {
					giver.message(t.getTheName() + " asks for "
							+ Coin.valueString(value) + " to repair your "
							+ gift.getName(Game.hero()));
					giver.message("Do you accept? (y/n)");
					char k = Game.getOption("ynq");
					if (k == 'y') {
						Game.messageTyrant(
								"You gave " + gift.getName(Game.hero())
										+ " and " + Coin.valueString(value)
										+ " to the smith for repair.");

						Coin.removeMoney(giver, value);

						// fix the item status.
						// gift.set("Damage",false);
						gift.set("HPS", gift.getStat("HPSMAX"));
						gift.getTheName(); // force update status;
						// System.out.println(gift.getTheName());
					} else {
						// you don't want to pay?
						giver.message("Don't waste my time then!");
					}
				} else {
					// It has no value!
					giver.message("What a piece of junk! Don't waste my time!");
				}
			} else {
				// not damaged or no gift!
				giver.message("What on earth am I meant to do with that!");
			}

			return true;
		}
	}

	private static class IdScript extends Script {

		/*
		 * @author tomdemuyt
		 */

		// private static final long serialVersionUID = -6588466277758378986L;
		@Override
		public boolean handle(Thing t, Event e) {
			// Thing t is the identifier
			Thing gift = e.getThing("Gift");
			Thing giver = e.getThing("Giver");

			if (t.toString().equals("jeweller")) {
				if (!gift.getFlag("IsRing") && !gift.getFlag("IsNecklace")) {
					Game.messageTyrant(
							"I am sorry, I only know about rings and necklaces.");
					return false;
				}
			}

			int value = Item.value(gift); // value of the thing
			int hskill = giver.getStat("CH") * giver.getStat(Skill.TRADING); // Trader
																				// skill
			int sskill = t.getStat("CH");

			System.out.println(200 * (hskill / (hskill + sskill)));
			System.out.println(t.toString());
			// for debugging
			// value=(int)(0.3*(value*0.3+0.7*value*(hskill/(hskill+sskill))));
			value = 200;

			giver.message(
					t.getTheName() + " asks for " + Coin.valueString(value)
							+ " to identify your " + gift.getName(Game.hero()));
			giver.message("Do you accept? (y/n)");
			char k = Game.getOption("ynq");
			if (k == 'y') {
				Game.messageTyrant("You gave " + Coin.valueString(value)
						+ " to the " + t.toString() + " to identify the "
						+ gift.getName(Game.hero()) + ".");

				Coin.removeMoney(giver, value);

				// Identify the item
				Item.identify(gift);
				Game.messageTyrant(t.getTheName() + " identifies it as "
						+ gift.getAName() + ".");
			} else {
				// you don't want to pay?
				giver.message("Don't waste my time then!");
			}

			return true;
		}
	}

	private static class TeachingScript extends Script {
		private static final long serialVersionUID = 3760564208726914357L;

		@Override
		public boolean handle(Thing t, Event e) {
			Thing giver = e.getThing("Target");

			int cost = giver.getStat(RPG.ST_SKILLPOINTSSPENT) * 100
					* giver.getLevel();
			cost = (int) (cost / (1.0 + 0.3 * giver.getStat(Skill.TRADING)));

			if (giver.getStat(RPG.ST_SKILLPOINTS) <= 0) {
				final String[] needMoreExperience = {
						"You need to gain experience before I can teach you any more!",
						"Sorry, I can't teach you any more now - you need to gain experience before.",
						"I can't teach you any more now, sorry - you need to gain experience before.",
						"Unfortunately, I can't teach you any more now - you need to gain experience before." };
				Game.messageTyrant("\"" + RPG.pick(needMoreExperience) + "\"");
				return true;
			}

			if (cost > 0) {
				Game.messageTyrant(t.getTheName() + " asks for "
						+ Coin.valueString(cost) + " for training.");

				// enough cash?
				if (Coin.getMoney(giver) < cost) {
					Game.messageTyrant("You can't afford that!");
					return true;
				}

				Game.messageTyrant("Do you agree? (y/n)");
				if (!(Game.getOption("yn") == 'y')) {
					return true;
				}
			} else {
				final String phrases[] = {
						"I like you, my friend; I can give you some free training to improve your skills.",
						"My friend, I can give you some free training to improve your skills.",
						"I like you; I can give you some free training to improve your skills.",
						"I like you; I offer some free training to improve your skills.",
						"I can give you some free training to improve your skills.",
						"Well, I can give you some free training to improve your skills." };
				Game.messageTyrant("You talk to " + t.getTheName());
				Game.messageTyrant("\"" + RPG.pick(phrases) + "\"");
				Game.messageTyrant("Do you agree? (y/n)");
				if (!(Game.getOption("yn") == 'y')) {
					final String comeBackLater[] = {
							"Come back later if you change your mind!",
							"You can come back later if you change your mind!",
							"You don't want FREE training? Anyway, come back later if you change your mind.",
							"You don't want training for FREE? What a guy :-) !" };
					Game.messageTyrant("\"" + RPG.pick(comeBackLater) + "\"");
					return true;
				}
			}

			// Oh, the obscenity, the work from String[] to ArrayList
			ArrayList sks = new ArrayList(
					Arrays.asList(Skill.getTrainableSkills(giver, true))); // Skill.getList(giver);
			ArrayList sksr = new ArrayList(
					Arrays.asList(Skill.getTrainableSkills(giver, false))); // Skill.getUnmarkedList(giver);
			// String test[] = Skill.getTrainableSkills( giver , true );
			String s =
					Game.selectString("Select a skill to improve:", sks, sksr);

			if (s != null) {
				Skill.train(giver, s);
				Coin.removeMoney(giver, cost);
			} else {
				final String laterThen[] =
						{ "Maybe later then!", "Well, maybe later then.",
								"Anyway, you can come back later." };
				Game.messageTyrant("\"" + RPG.pick(laterThen) + "\"");
			}
			return true;
		}

	}

	private static class RiddleScript extends Script {
		private static final long serialVersionUID = 2254796328898345150L;

		private static ArrayList riddles = null;

		private static ArrayList answers = null;

		private static void setupRiddles() {
			riddles = new ArrayList();
			answers = new ArrayList();
			riddles.add(
					"What walks on four legs in the morning, two in the afternoon and three in the evening?");
			answers.add("man");
			riddles.add(
					"What starts with 'e', ends with 'e' and contains one letter?");
			answers.add("envelope");
			riddles.add("What has eyes but cannot see?");
			answers.add("potato");
			riddles.add(
					"Born at the same time as the world, destined to live as long as the world, and yet never five weeks old.  What is it?");
			answers.add("moon");
			riddles.add(
					"It wasn't my sister, nor my brother, but still was the child of my father and mother.  Who was it?");
			answers.add("you");
			riddles.add(
					"What can be measured, but has no length, width or height?");
			answers.add("temperature");
			riddles.add("What do all living things do at the same time?");
			answers.add("age");
			riddles.add("What do you break with just one word?");
			answers.add("silence");
			riddles.add("What can't you see that is always before you?");
			answers.add("future");
			riddles.add(
					"Which room has no door, no windows, no floor and no roof?");
			answers.add("mushroom");
			riddles.add("What is always coming, but never arrives?");
			answers.add("tomorrow");
			riddles.add("What is often returned but never borrowed?");
			answers.add("thanks");
			riddles.add("What is bought by the yard and worn by the foot?");
			answers.add("carpeting");
			riddles.add("What is full of holes and yet holds water?");
			answers.add("sponge");
			riddles.add(
					"What lives on its own substance and dies when it devours itself?");
			answers.add("candle");
			riddles.add(
					"What 5 letter word can have its last 4 letters removed and still sound the same?");
			answers.add("queue");
			riddles.add("What gets harder to catch the faster you run?");
			answers.add("breath");
			riddles.add("What doesn't exist, but has a name?");
			answers.add("nothing");
			riddles.add(
					"The more there is of this, the less you see of it. What is it?");
			answers.add("darkness");
			riddles.add(
					"What is black when you buy it, red as you use it, and grey when you throw it out?");
			answers.add("coal");
			riddles.add(
					"The more you take, the more you leave behind. What are they?");
			answers.add("footsteps");
			riddles.add(
					"What occurs once in a minute, twice in a moment, but never in an hour?");
			answers.add("m");
			riddles.add("What grows bigger the more you take from it?");
			answers.add("hole");
			riddles.add(
					"What five-letter word becomes shorter when you add two letters to it?");
			answers.add("short");
			riddles.add("What do you lose every time you stand up?");
			answers.add("lap");
		}

		static {
			setupRiddles();
		}

		@Override
		public boolean handle(Thing t, Event e) {
			Thing responder = e.getThing("Target");

			String missedStat =
					Text.camelizeString(t.getName()) + "RiddleMissed";
			String solvedStat =
					Text.camelizeString(t.getName()) + "RiddleSolved";
			Game.messageTyrant("Camel:  " + solvedStat);
			int failures = responder.getStat(missedStat);
			if (failures > 2) {
				// only three misses per game
				Game.messageTyrant(t.getTheName()
						+ " is no longer willing to talk with you.");
				return true;
			} else if (failures > 0) {
				Game.messageTyrant(
						t.getTheName() + " has another question for you.");
			} else {
				Game.messageTyrant(t.getTheName() + " has a question for you.");
			}
			Game.messageTyrant("Would you like to answer the question? (y/n)");
			char accept = Game.getOption("yn");
			if (accept == 'y') {
				int riddleIndex = RPG.r(riddles.size());
				String riddle = "\"" + (String) riddles.get(riddleIndex) + "\"";
				String answer = (String) answers.get(riddleIndex);
				Game.messageTyrant(riddle);
				String response = Game.getLine("Your answer: ");
				if (response.equalsIgnoreCase(answer)) {
					Game.messageTyrant("\"Well done!\"");
					responder.incStat(solvedStat, 1);
					return true;
				}
				Game.messageTyrant(
						"\"Thinking must not be your strong suit.\"");
				responder.incStat(missedStat, 1);
				return true;
			}
			responder
					.message("Come back when you want to test your knowledge!");
			return true;
		}
	}

	public static void initRaces() {
		Thing t;

		// humans are the most common inhabitants in the world of Tyrant
		// they are good all-round characters
		t = Lib.extend("human", "base person");
		Monster.stats(t, 7, 7, 7, 7, 7, 7, 7, 7);
		t.set("Race", t.name());
		t.set("Image", 101);
		t.set("RaceDescription",
				"Adaptable and inventive, humans are the most common race on the surface of the world. They live in communities ranging in size from small villages to great fortified cities.");
		t.incStat(RPG.ST_SKILLPOINTS, 1);
		Lib.add(t);

		// dwarves are sturdy and industrious cave dwellers
		// they are famed for their skill in smithing and mining
		// they are not very nimble however, and are less good at magic
		t = Lib.extend("dwarf", "base person");
		Monster.stats(t, 6, 7, 2, 11, 5, 8, 4, 10);
		t.set("Race", t.name());
		t.set("Image", 83);
		t.incStat(RPG.ST_MOVESPEED, -10);
		t.multiplyStat(RPG.ST_RECHARGE, 0.3);
		t.incStat(Skill.MAGICRESISTANCE, 1);
		t.incStat(Skill.SMITHING, RPG.r(2));
		t.incStat(Skill.MINING, RPG.r(3));
		t.set("RaceDescription",
				"Delving in the deep underworld or in the high mountains, dwarves are hardy folk with a fierce tradition of pride and loyalty. Above all, they prize gold, gems and precious metals which they accumulate in their cavernous underground halls. Suspicious of other races in genral, dwarves prefer to keep to themselves, although they have forged trading links with humans. They have a particular dislike for elves, since they have still not forgotten ancient conflicts that once brought the two races to war.");
		Lib.add(t);

		// hobbits are just three feet high
		// they are peaceful folk, renowned as farmers
		t = Lib.extend("hobbit", "base person");
		Monster.stats(t, 8, 4, 6, 3, 6, 12, 9, 7);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.incStat(Skill.FARMING, 1);
		t.incStat(Skill.HERBLORE, RPG.r(3));
		t.incStat(Skill.COOKING, 1);
		t.incStat(Skill.STORYTELLING, RPG.r(2));
		t.set("RaceDescription",
				"Hobbits are a diminutive folk, half the height of the average man, with large hairy feet. What they lack in strength and stamina they make up for in skill, cunning and a good-natured charm. Typically farmers and herb-gardeners, they generally shun adventuring ways - the peaceful, carefree hobbit likes nothing better than relaxing after a hearty meal, with a puff of pipeweed and a good story told among friends. Occasionally however, a hobbit succumbs to wanderlust, the lure of gold, or simply becomes involved in events beyond his control...");
		Lib.add(t);

		// elves are wise, skilled and agile
		// they lack toughness and strength, however
		t = Lib.extend("base elf", "base person");
		Monster.stats(t, 8, 5, 8, 4, 7, 6, 6, 5);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.multiplyStat(RPG.ST_RECHARGE, 1.3);
		t.incStat(RPG.ST_SPEED, 10);
		t.incStat(Skill.LITERACY, 1);
		Lib.add(t);

		// high elves are a noble breed of elf
		t = Lib.extend("high elf", "base elf");
		Monster.stats(t, 8, 5, 7, 4, 10, 7, 12, 9);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.set("RaceDescription",
				"The high elves are the most ancient of all races, and enjoy enormously long lives which they dedicate by and large to noble pastimes such as dancing, writing, art and music. Their finely developed culture and traditions remains impenetrable to most other races, who often regard them as aloof and arrogant. High elves ally most frequenctly with man, who they seem to regard affectionately as unruly younger siblings in need of guidance. The dark elves are their bitterest foes, separated. In battle, high elves tend to rely on speed, agility and magic rather than brute force.");
		t.incStat(Skill.LITERACY, 1);
		t.incStat(Skill.PERCEPTION, 1);
		Lib.add(t);

		// dark elves are corrupted by evil, vicious and powerful
		// they prefer throwing weapons, darts and shurikens
		t = Lib.extend("dark elf", "base elf");
		Monster.stats(t, 9, 6, 7, 6, 8, 7, 5, 5);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.set("RaceDescription",
				"The typical Dark Elf has fine white hair like spider silk, lustrous skin like graphite and glossy black eyes like obsidian. Although the High and Dark Elves deny they are related to one another, the truth is that the Dark Elves were born of a schism from the original Elven race millennia ago. The exact nature of the split is uncertain since neither race will admit it ever happened; whatever it was, the Dark Elves left their forest homes in Uendil and made their way underground, forming their own empire beneath the mountains of Azak-Hal; since then they have spread to other regions. Like high elves, they are skilled and agile. The harsh environment has made the Dark Elves more well-rounded; they are stronger in body and will, although less intelligent and crafty. They are ferocious in combat and adept in the use of throwing weapons.");
		t.incStat(Skill.THROWING, 1);
		t.incStat(Skill.FEROCITY, 1);
		Lib.add(t);

		// wood elves are agile. excellent archers
		// they prefer to live in isolation from other folk
		t = Lib.extend("wood elf", "base elf");
		Monster.stats(t, 10, 4, 12, 4, 8, 6, 8, 8);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.set("RaceDescription",
				"Wood elves live in harmony with nature, and in general shun the company of other races. They thrive on songs and storytelling, their ancient culture having grown in woodland relams for thousands of years. They are more light hearted and mischeivous than the austere High Elves, but their common ancestry makes them frequent allies.");
		t.incStat(Skill.ARCHERY, 1);
		Lib.add(t);

		// gnomes are industrious little beings
		// they make excellent traders and tinkerers
		t = Lib.extend("gnome", "base person");
		Monster.stats(t, 6, 3, 4, 3, 8, 9, 8, 12);
		t.set("Race", t.name());
		t.set("Image", 10);
		t.incStat("VisionRange", -1);
		t.incStat(Skill.TRADING, RPG.d(2));
		t.incStat(Skill.IDENTIFY, RPG.d(2));
		t.set("RaceDescription",
				"Gnomes are the smallest of all the humanoid races, with adults rarely exceeding two feet in height. Forced to live by their wits rather than their strength, Gnomes have found their talent in tinkering and trade. More traditional gnomes live in well-hidden and defended gnomish towns in the forests or hills, but others have settled along humans and dwarves where their crafting skills and ingenuity enable them to earn a good living whilst also enjoying the protection of their larger cousins. Some gnomish businessmen have become so successful their their fortunes rival those of human princes, and they are thus able to wield considerable political power.");
		t.incStat(RPG.ST_MOVESPEED, -20);
		t.incStat(RPG.ST_ATTACKSPEED, -10);
		t.incStat("Luck", 7);
		Lib.add(t);

		// half orcs are aggressive and strong
		t = Lib.extend("half orc", "base person");
		Monster.stats(t, 7, 11, 8, 8, 5, 7, 3, 4);
		t.set("DeathDecoration", "slime pool");
		t.set("Race", t.name());
		t.set("Image", 244);
		t.set("IsGoblinoid", 1);
		t.incStat(RPG.ST_SKILLPOINTS, -1);
		t.incStat(Skill.FEROCITY, 1);
		t.incStat(Skill.UNARMED, 1);
		t.incStat(Skill.BRAVERY, 1);
		t.set("RaceDescription",
				"A fearsome and tragic half-breed between man and orc, half orcs usually find themselves shunned by both races. Bitter and angry, many take to banditry or hired work as mercenaries, where their agressive tendencies coupled with smarter wits than any orc make them formidable fighters.");
		Lib.add(t);

		// half trolls are phenomenally strong
		// ... but very stupid
		t = Lib.extend("half troll", "base person");
		Monster.stats(t, 6, 16, 4, 11, 1, 8, 2, 2);
		t.set("DeathDecoration", "slime pool");
		t.set("Race", t.name());
		t.set("Image", 244);
		t.set("IsGoblinoid", 1);
		t.incStat(RPG.ST_SKILLPOINTS, -1);
		t.incStat(Skill.UNARMED, 1);
		t.incStat(Skill.SURVIVAL, 1);
		t.multiplyStat(RPG.ST_REGENERATE, 3);
		t.set("RaceDescription",
				"Half trolls are a mutant breed related to trolls and goblins. With limited intelligence, they often live in the wild as hunters and scavengers. They are freqently also found among orcs and goblins, where their great muscle is highly valued by the contantly feuding factions.");
		Lib.add(t);

		// argonians are tough, smart and agile but fairly weak
		t = Lib.extend("argonian", "base person");
		Monster.stats(t, 5, 4, 9, 12, 10, 8, 2, 7);
		t.set("DeathDecoration", "slime pool");
		t.set("Race", t.name());
		t.set("Image", 423);
		t.set("RaceDescription",
				"Argonians are the offspring of the unholy union of humans and snakes. Their skin has a greenish hue and a scaly toughness. They are invulnerable to poison but are extra sensitive to cold, greatly prefering warm climates. They are very tough. Shunned by most of society they tend to prefer solitary professions.");
		t.incStat(Skill.SWIMMING, 2);
		t.incStat("ARM", 3);
		t.set("RES:poison", 1000); // immune to poison
		t.set("RES:ice", -10); // vulnerable to cold
		t.set("RES:fire", 10); // resistant to fire
		t.multiplyStat(RPG.ST_REGENERATE, 2);
		Lib.add(t);

		// hawken are very agile and fast. They have exceptional eyesight.
		t = Lib.extend("hawken", "base person");
		Monster.stats(t, 8, 4, 15, 5, 8, 7, 5, 5);
		t.set("DeathDecoration", "blood pool");
		t.set("Race", t.name());
		t.set("Image", 420);
		t.incStat("VisionRange", 4);
		t.set("RaceDescription",
				"The race of hawken decended from couplings between men and harpies. Birdmen have sharp hawklike features and soft downy skin. They have phenomenal eyesight and perception. They are preternaturally quick.");
		t.incStat(Skill.ATHLETICS, RPG.d(3));
		t.incStat(Skill.PERCEPTION, RPG.r(3));
		t.incStat(Skill.ALERTNESS, RPG.d(3));
		t.set("RES:piercing", -5); // vulnerable to arrows, spears
		t.set("RES:impact", -5); // vulnerable to being bludgeoned
		Lib.add(t);

		// The great thinkers of the world, but you can knock them down with a
		// feather.
		// MA: have reduced skills slightly - otherwise too powerful once they
		// manage to increase ST/TG
		t = Lib.extend("pensadorian", "base person");
		Monster.stats(t, 5, 3, 5, 3, 15, 9, 5, 10);
		t.set("DeathDecoration", "blood pool");
		t.set("Race", t.name());
		t.set("Image", 101);
		t.incStat("VisionRange", -2);
		t.set("RaceDescription",
				"The pensadorians' ancestors were human, but centuries of selective breeding in a society that valued nothing but intelligence has caused them to slowly evolve into a different race.  They have inherently brilliant minds but barely have the muscularity to hold their heads up.");
		t.incStat(Skill.LITERACY, RPG.d(2));
		t.incStat(Skill.IDENTIFY, RPG.r(2));
		t.incStat(Skill.RUNELORE, RPG.r(2));
		// t.incStat(Skill.LANGUAGES, 1);
		// t.incStat(Skill.STRATEGY, 1);
		t.set("RES:poison", -5); // vulnerable to poison
		t.set("RES:ice", -5); // vulnerable to cold
		t.set("RES:fire", -5); // vulnerable to fire
		t.set("RES:shock", -5); // vulnerable to shock
		t.set("RES:acid", -5); // vulnerable to acid
		t.set("RES:piercing", -5); // vulnerable to arrows, spears
		t.set("RES:impact", -5); // vulnerable to being bludgeoned
		Lib.add(t);
	}

	public static void initWanderers() {
		Thing t;

		t = Lib.extend("merchant", "human");
		Monster.strengthen(t, 8);
		t.set("IsWanderer", 1);
		t.set("Image", 54);
		t.set("LevelMin", 5);
		t.set("Frequency", 50);
		Lib.add(t);

		t = Lib.extend("jeweller", "gnome");
		Monster.strengthen(t, 8);
		t.set("IsWanderer", 1);
		t.set("Image", 54); // Test , this was 36
		t.set("LevelMin", 5);
		t.set("Frequency", 50);
		t.set("OnGift", new IdScript());
		t.set("Introduction",
				"I am a jeweler, I identify rings and amulets at a price.");
		Lib.add(t);

		t = Lib.extend("wandering teacher", "schoolmaster");
		t.set("IsWanderer", 1);
		t.set("LevelMin", 10);
		t.set("Frequency", 50);
		Lib.add(t);

	}

	public static void initUniversityStaff() {
		Thing t;

		t = Lib.extend("base university person", "human");
		Monster.stats(t, 12, 14, 12, 28, 27, 37, 14, 22);
		t.set("IsUniversityStaff", 1);
		t.set("ARM", 2);
		t.set("Level", 9);
		t.set("Luck", 50);
		Lib.add(t);

		t = Lib.extend("professor", "base university person");
		t.set(Skill.CASTING, 5);
		t.set("Image", 142);
		Lib.add(t);

		t = Lib.extend("advisor", "base university person");
		t.set(Skill.CASTING, 3);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("dean of admissions", "base university person");
		t.set(Skill.CASTING, 7);
		t.set("Image", 141);
		{
			Script s = new RiddleScript();
			t.set("OnChat", s);
		}
		Lib.add(t);

		t = Lib.extend("dean of thaumaturgy", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("dean of alchemy", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("dean of herbology", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("dean of theology", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("dean of symbolism", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 145);
		Lib.add(t);

		t = Lib.extend("bursar", "base university person");
		t.set("Image", 143);
		t.set(Skill.CASTING, 3);
		Lib.add(t);

		t = Lib.extend("student", "base university person");
		t.set(Skill.CASTING, 1);
		t.set("Image", 148);
		Lib.add(t);

		t = Lib.extend("gardner", "base university person");
		t.set("Image", 146);
		Lib.add(t);

		t = Lib.extend("janitor", "base university person");
		t.set("Image", 147);
		Lib.add(t);

		t = Lib.extend("clerk", "base university person");
		t.set("Image", 150);
		Lib.add(t);

		t = Lib.extend("campus policeman", "base university person");
		Monster.stats(t, 12, 35, 35, 28, 27, 37, 14, 22);
		t.set("Image", 81);
		Lib.add(t);

		t = Lib.extend("provost", "base university person");
		t.set(Skill.CASTING, 11);
		t.set("Image", 125);
		Lib.add(t);

		t = Lib.extend("university president", "base university person");
		t.set(Skill.CASTING, 15);
		t.set("Image", 149);
		Lib.add(t);

	}

	public static void initDwarves() {
		Thing t;

		t = Lib.extend("dwarf clansman", "dwarf");
		t.set("Introduction", "Welcome to our stronghold stranger!.");
		t.set("LevelMin", 3);
		Lib.add(t);

		t = Lib.extend("dwarf guard", "dwarf clansman");
		Monster.strengthen(t, 2.0);
		t.set("DefaultThings", "[IsWeapon],[IsArmour],[IsCoin]");
		t.set("Introduction",
				"I am a dwarven guard, I protect this stronghold and its' riches.");
		t.set("LevelMin", 7);
		Lib.add(t);
	}

	public static void initWoodFolk() {
		Thing t;

		t = Lib.extend("woodsman", "human");
		t.set("Image", 114);
		Monster.stats(t, 8, 16, 10, 18, 7, 17, 8, 12);
		t.set("Level", 3);
		t.set("ARM", 2);
		t.set("Introduction", "Welcome to the Wood Temple, Stranger!");
		Lib.add(t);

		t = Lib.extend("wood temple guard", "human");
		t.set("Image", 115);
		Monster.stats(t, 18, 18, 13, 28, 9, 27, 12, 14);
		t.set("ARM", 4);
		t.set("Level", 8);
		t.set("Introduction",
				"Dont kill or steal. Teaching classes are given by the archers, you can donate for the new Temple with the priests");
		t.set("DefaultThings", "[IsBow], 20 [IsArrow],[IsWeapon]");
		Lib.add(t);

		t = Lib.extend("wood temple archer", "human");
		t.set("Image", 128);
		Monster.stats(t, 38, 18, 33, 28, 19, 37, 16, 24);
		t.set("ARM", 5);
		t.set("Level", 13);
		t.set("DefaultThings", "[IsBow], 20 [IsArrow],[IsWeapon]");
		{
			Script s = new TeachSkillScript();
			s.set("SkillName", Skill.ARCHERY);
			s.set("Price", 2000);
			s.set("Practitioner", "archer");
			t.set("OnChat", s);
		}
		Lib.add(t);

		t = Lib.extend("wood priest", "human");
		t.set("Image", 125);
		Monster.stats(t, 12, 14, 12, 28, 27, 37, 14, 22);
		t.set("ARM", 2);
		t.set("Level", 9);
		t.set("DefaultThings", "[IsBow], 20 [IsArrow], Poison Cloud, Fireball");
		t.set(Skill.CASTING, 3);
		t.set("Luck", 50);
		t.set("RES:poison", 50);
		t.set("Introduction",
				"I'm collecting funds for the wood temple, so I am selling some old scrolls.");
		t.set("OnGift", SwapScript.create("IsCoin",
				RPG.d(2) == 1 ? "IsScroll" : "IsIdentifyScroll", 6));
		Lib.add(t);

		t = Lib.extend("wood priestess", "wood priest");
		t.set("Image", 126);
		Monster.strengthen(t, 2);
		t.set("Introduction",
				"Hi, welcome to the wood temple. If you're looking for scrolls, talk to the wood priests.");
		t.set("Level", 14);
		t.set("Luck", 100);
		t.set("ARM", 12);
		Lib.add(t);
	}

	public static void initTownies() {
		Thing t;

		t = Lib.extend("base townie", "human");
		t.set("DefaultThings", "[IsItem]");
		Lib.add(t);

		t = Lib.extend("village girl", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_PLEASANT));
		t.set("Introduction",
				"Hi, I collect rings, I will trade them for potions.");
		t.set("OnGift", SwapScript.create("IsRing", "IsPotion", 12));
		t.set("Image", 106);
		Lib.add(t);

		t = Lib.extend("farmer", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Image", 101);
		Lib.add(t);

		t = Lib.extend("villager", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Image", 100);
		Lib.add(t);

		t = Lib.extend("village woman", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Introduction", "Hi, if you're hungry, give me some coin.");
		t.set("Image", 103);
		t.set("OnGift", SwapScript.create("IsCoin", "IsFood", 20));
		Lib.add(t);

		t = Lib.extend("townswoman", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Image", 103);
		Lib.add(t);

		t = Lib.extend("townie", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Image", 100);
		t.set("DefaultThings", "[IsWeapon]");
		Lib.add(t);

		t = Lib.extend("boy", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_TOWNIE));
		t.set("Image", 31);
		t.set("DefaultThings", "[IsFood]");
		Lib.add(t);

		t = Lib.extend("ranger", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_PLEASANT));
		Monster.strengthen(t, 3);
		t.set("ARM", 5);
		t.set("Image", 128);
		t.incStat(Skill.ARCHERY, 3);
		t.set("DefaultThings", "[IsWeapon],[IsBow],[IsArrow],[IsArrow]");
		t.set("Level", 10);
		t.set("Introduction",
				"Hi, I'm the town's ranger. I have arrows for sale.");
		t.set("OnGift", SwapScript.create("IsCoin", "IsArrow", 6));
		Lib.add(t);

		t = Lib.extend("guard", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 3);
		t.set("Level", 7);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.DEFENCE, 2);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Monster.strengthen(t, 3);
		Lib.add(t);

		t = Lib.extend("priest", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GOODLY));
		Monster.strengthen(t, 4);
		t.set(Skill.DEFENCE, 2);
		t.set("Image", 124);
		t.set("DeathDecoration", "holy ghost");
		Lib.add(t);

		t = Lib.extend("wizard", "base townie");
		t.setPersonality(
				new Personality(Personality.CHATTER, Personality.CHATTER_WISE));
		Monster.strengthen(t, 4);
		t.set("Image", 121);
		t.set(Skill.CASTING, 3);
		t.set("ARM", 4);
		t.set("Introduction",
				"Hi, I'm the town's wizard. I have wands for sale.");
		t.set("OnGift", SwapScript.create("IsCoin", "IsWand", 16));
		t.set("DefaultThings", "[IsSpellBook],Magic Missile,Fireball");
		Lib.add(t);

		t = Lib.extend("teacher", "base townie");
		t.set("Image", 122);
		t.setPersonality(new Personality(Personality.TEACHER, 0));
		t.set("OnChat", new TeachingScript());
		Lib.add(t);

		t = Lib.extend("schoolmaster", "base townie");
		t.set("Image", 122);
		t.setPersonality(new Personality(Personality.TEACHER, 0));
		t.set("OnChat", teachSkillScript(Skill.LITERACY, 5000));
		Lib.add(t);

		//
		// Soliders
		//

		t = Lib.extend("young soldier", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 1);
		t.set("Level", 1);
		t.set(Skill.UNARMED, 1);
		t.set(Skill.DEFENCE, 1);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Lib.add(t);

		t = Lib.extend("seasoned soldier", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 3);
		t.set("Level", 3);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.DEFENCE, 2);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Lib.add(t);

		t = Lib.extend("border legionnaire", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 3);
		t.set("Level", 5);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.DEFENCE, 2);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Monster.strengthen(t, 2);
		Lib.add(t);

		t = Lib.extend("garrison commander", "base townie");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 3);
		t.set("Level", 10);
		t.set(Skill.UNARMED, 3);
		t.set(Skill.DEFENCE, 3);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Monster.strengthen(t, 3);
		Lib.add(t);

		t = Lib.extend("elven ranger", "high elf");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GUARD));
		t.set("Image", 111);
		t.set("ARM", 3);
		t.set("Level", 8);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.DEFENCE, 2);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Monster.strengthen(t, 3);
		Lib.add(t);

		//
		// End Soldiers
		//

		/*
		 * @since 13,Aug,04 by ppirrip Try to make the blacksmith 'prepair'
		 * something. I am thinking to add a new 'repair' script.
		 */
		t = Lib.extend("blacksmith", "human");
		// add new stuff
		t.setPersonality(new Personality(Personality.BLACKSMITH, 0));
		{
			Script s = new TeachSkillScript();
			s.set("SkillName", Skill.WEAPONLORE);
			s.set("Price", 3000);
			t.set("OnChat", s);
		}
		t.set("OnGift", new RepairScript());
		// end new stuff
		t.set("Image", 109);
		t.set("Level", 10);
		t.set(Skill.ATTACK, 3);
		t.set(Skill.DEFENCE, 2);
		t.set("DefaultThings", "steel battle axe,[IsArmour]");
		Monster.strengthen(t, 6);
		Lib.add(t);

		t = Lib.extend("shopkeeper", "human");
		// t.setPersonality(new Personality(Personality.CHATTER,
		// Personality.CHATTER_SHOPKEEPER));
		t.set("Image", 108);
		t.set("IsShopkeeper", 1);
		t.set("IsDisplaceable", 0);
		t.set("Level", 15);
		t.set("OnGift", new SellingScript());
		t.set("OnChat", new ListSellingScript());
		t.set("DefaultThings",
				"[IsWeapon],[IsArmour],[IsOffensiveSpell],[IsSummonSpell]");
		t.set(Skill.ATTACK, 3);
		t.set(Skill.DEFENCE, 3);
		t.set(Skill.CASTING, 3);
		Monster.strengthen(t, 6);
		Lib.add(t);

		t = Lib.extend("trader", "human");
		t.set("Image", 108);
		t.set("Level", 15);
		t.set("OnChat", new ListSellingScript());
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Monster.strengthen(t, 4);
		Lib.add(t);

		/**
		 * Pip Pirrip 30-07-04 Add teacher personality to healer
		 */
		t = Lib.extend("healer", "human");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_SHOPKEEPER));
		Monster.strengthen(t, 4);
		t.set("Level", 18);
		t.set("Image", 124);
		t.set("OnGift", new SellingScript());
		t.set("DefaultThings", "[IsPotion]");
		t.set("DeathDecoration", "holy ghost");
		Lib.add(t);

		t = Lib.extend("learned sage", "human");
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_SHOPKEEPER));
		Monster.strengthen(t, 2);
		t.set("Level", 12);
		t.set("Image", 125);
		{
			Script s = new TeachSkillScript();
			s.set("SkillName", Skill.LITERACY);
			s.set("Price", 5000);
			t.set("OnChat", s);
		}
		t.set("DefaultThings", "[IsPotion]");
		t.set("DeathDecoration", "holy ghost");
		Lib.add(t);

		// goblin shopkeeper extends goblin shaman
		t = Lib.extend("goblin shopkeeper", "goblin shaman");
		t.set("IsHostile", 0);
		t.set("IsInhabitant", 1);
		t.set("IsDisplaceable", 0);
		t.set("IsShopkeeper", 1);
		t.set("IsPerson", 1);
		t.setPersonality(new Personality(Personality.CHATTER,
				Personality.CHATTER_GOBLIN));
		Lib.add(t);
	}

	public static void initNPCs() {
		Thing t;

		t = Lib.extend("Jolly Old Nyck", "human");
		// AI.name(t, "Jolly Old Nyck");
		t.set("Image", 112);
		t.set("OnChat", new Script() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean handle(Thing t, Event e) {
				Thing tt = e.getThing("Target");

				Game.messageTyrant("\"Yo Ho Ho!\"");
				Game.messageTyrant("\"'Tis the season to be jolly\"");
				Game.messageTyrant("\"Here's a present for ye!\"");

				Thing th = Lib.createArtifact(50);
				Game.messageTyrant(t.getName() + " gives you " + th.getAName());
				tt.addThing(th);

				Game.messageTyrant(t.getName() + " suddenly dissapears!");

				t.remove();
				return true;
			}

		});
		Lib.add(t);
	}
}