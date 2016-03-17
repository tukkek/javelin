package javelin.view.screen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.action.world.CastSpells;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.spell.Summon;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.model.world.WorldMap;
import javelin.model.world.place.Dungeon;
import javelin.model.world.place.Haxor;
import javelin.model.world.place.Lair;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.town.Town;
import javelin.view.SquadScreen;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * @see Haxor
 * @author alex
 */
public class HaxorScreen extends SelectScreen {
	transient private static final ArrayList<String> QUOTES =
			new ArrayList<String>();

	/**
	 * A curiosity: I had no intention of having seven different types of
	 * {@link Keys} in the game - they were a late 1.1 idea to give more of a
	 * long-term goal to the game. Fuck, I didn't even plan for {@link Town}s to
	 * have any sort of theme at all - they were just supposed to be random
	 * collections of upgrades and items that eventually got to big to be purely
	 * random (or it would be a pain in the ass to keep track of what is where
	 * in each game) - but in the end is there anything more win than Helloween?
	 * So I had to make the best out of this coincidence :)
	 * 
	 * @author alex
	 */
	String[] WINMESSAGES = new String[] {
			"Make the people\nHold each other's hands\nAnd fill their hearts with truth\nYou made up your mind\nSo do as divined\n",
			"Put on your armour\nRagged after fights\nHold up your sword\nYou're leaving the light\nMake yourself ready\nFor the lords of the dark\nThey'll watch your way\nSo be cautious, quiet and hark\n",
			"You hear them whispering\nIn the crowns of the trees\nYou're whirling 'round\nBut your eyes don't agree\nWill'o'the wisps\nMisguiding your path\nYou can't throw a curse\nWithout takin' their wrath\n",
			"Watch out for the seas of hatred and sin\nOr all us people forget what we've been\nOur only hope's your victory\nKill that Satan who won't let us be--kill!\n",
			"You're the Keeper of the Seven Keys\nThat lock up the seven seas\nAnd the Seer of Visions said before he went blind\nHide them from demons and rescue mankind\nOr the world we're all in will soon be sold\nTo the throne of the evil paid with Lucifer's gold\n",
			"You can feel cold sweat\nRunning down your neck\nAnd the dwarfs of falseness\nThrow mud at your back\n",
			"Guided by spells\nOf the old Seer's hand\nYou're suffering pain\nOnly steel can stand\n",
			"Stay well on your way and follow the sign\nFulfill your own promise and do what's divined\nThe seven seas are far away\nPlaced in the valley of dust, heat and sway\n",
			"You're the Keeper of the Seven Keys\nThat lock up the seven seas\nAnd the Seer of Visions said before he went blind\nHide them from demons and rescue mankind\nOr the world we're all in will soon be sold\nTo the throne of the evil paid with Lucifer's gold\n",
			"Throw the first key into the sea of hate\nThrow the second key into the sea of fear\nThrow the third key into the sea of senselessness\nAnd make the people hold each other's hands\nThe fourth key belongs into the sea of greed\nAnd the fifth into the sea of ignorance\nDisease, disease, disease my friend\nFor this whole world's in devil's hand\nDisease, disease, disease my friend\nThrow the key or you may die\n",
			"On a mound at the shore of the last sea\nHe is sitting, fixing your sight\nWith his high iron voice causing sickness\nHe is playing you out with delight\n\"Man who do you just think you are?\nA silly bum with seven stars\nDon't throw the key or you will see\nDimensions cruel as they can be\"\nDon't let him suck off your power\nThrow the key...!\n",
			"An earthquake, squirting fire, bursting ground\nSatan's screaming, and earth swallowing him away!\n",
			"You're the Keeper of the Seven Keys\nYou locked up the seven seas\nAnd the Seer of Visions can now rest in peace\nThere ain't no more demons and no more disease\nAnd, mankind, live up, you're free again\nYes the tyrant is dead, he is gone, overthrown\nYou have given our souls back to light\n\n    \"Keeper of the seven keys\" by the german power metal band Helloween\n", };

	static final Option GENERATELAIR = new Option("Generate lair", 2);
	static final Option GENERATEDUNGEON = new Option("Generate dungeon", 2);
	static final Option REBIRTH = new Option("Rebirth", 1);
	static final Option REMOVEABILITIY = new Option("Remove ability", 1);
	static final Option CHANGEAVATAR = new Option("Change unit avatar", 1);
	static final Option WIN = new Option("Win", 14);
	/**
	 * One-time offer to ressurect a fallen friendly {@link Combatant}.
	 */
	public static final Option RESSURECT =
			new Option("Ressurect fallen ally", 1);
	/**
	 * One-time offer to recruit one extra starting squad member from the
	 * starting character set.
	 * 
	 * This is an exception to the rule of not having balance-related features
	 * on Haxor's temple but some players may find the start of the game too
	 * slow - this would enable them to use their starting ticket to speed up
	 * the early game.
	 * 
	 * @see SquadScreen
	 */
	public static final Option SUMMONALLY = new Option("Summon ally", 1);

	private static final String DAILYQUOTE;

	static {
		QUOTES.add("Keep up with updates at http://javelinrl.wordpress.com :)");
		QUOTES.add(
				"Come discuss the game at http://reddit.com/r/javelinrl/ :)");
		QUOTES.add("Haxor's pet exclaims \"Kupo!\"");
		QUOTES.add("Haxor's pet exclaims \"Pika-pika!\"");
		QUOTES.add(
				"Haxor asks \"Do you feel like playing a round of Pachisi?\"");
		QUOTES.add(
				"Haxor's temple is unusually dark today... You are likely to be eaten by a grue.");
		QUOTES.add(
				"Haxor tells you again about how his son used to be an adventurer until he took an arrow to the knee.");
		QUOTES.add(
				"A night hag is visiting the Temple, she asks of you \"What can change the nature of a man?\"");
		QUOTES.add(
				"Haxor reminisces about one of his battle stories. He starts with \"War... War never changes...\"");
		QUOTES.add(
				"Haxor is working on an artifact he calls the Chrono Trigger.");
		Calendar now = Calendar.getInstance();
		String seed = "";
		seed += now.get(Calendar.DAY_OF_MONTH);
		seed += now.get(Calendar.MONTH) + 1;
		seed += now.get(Calendar.YEAR);
		DAILYQUOTE = QUOTES
				.get(new Random(seed.hashCode()).nextInt(QUOTES.size() - 1));
	}

	public HaxorScreen(String name, Town t) {
		super(name, t);
	}

	@Override
	public boolean select(Option o) {
		Combatant target = null;
		if (o.price > Haxor.singleton.tickets) {
			text += "\nNot enough tickets...";
			Javelin.app.switchScreen(this);
			return false;
		}
		if (o == CHANGEAVATAR || o == REMOVEABILITIY || o == REBIRTH) {
			ArrayList<String> members = new ArrayList<String>();
			for (Combatant c : Squad.active.members) {
				members.add(c.toString());
			}
			target = Squad.active.members.get(CastSpells
					.choose("Select a squad member:", members, true, true));
		}
		if (o.equals(CHANGEAVATAR)) {
			HashSet<String> avatars = new HashSet<String>();
			for (Monster m : Javelin.ALLMONSTERS) {
				avatars.add(m.avatarfile);
			}
			ArrayList<String> alphabetical = new ArrayList<String>(avatars);
			Collections.sort(alphabetical);
			int delta = 0;
			while (delta < alphabetical.size()) {
				text = "";
				for (int i = delta; i < delta + 9
						&& i < alphabetical.size(); i++) {
					text += "[" + (i - delta + 1) + "] " + alphabetical.get(i)
							+ "\n";
				}
				text += "\nPress ENTER to see more options or a number to select a new avatar.";
				Character feedback = print();
				if (feedback == '\n') {
					delta += 9;
					if (delta >= alphabetical.size()) {
						delta = 0;
					}
					continue;
				}
				try {
					final int index =
							Integer.parseInt(Character.toString(feedback));
					target.source.avatarfile =
							alphabetical.get(delta + index - 1);
					break;
				} catch (NumberFormatException e) {
					continue;
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		} else if (o.equals(SUMMONALLY)) {
			Haxor.singleton.options.remove(SUMMONALLY);
			ArrayList<Monster> candidates = SquadScreen.getcandidates();
			Collections.shuffle(candidates);
			float before = Squad.active.size();
			candidateloop: for (Monster candidate : candidates) {
				for (Combatant c : Squad.active.members) {
					if (candidate.name.equals(c.source.name)) {
						continue candidateloop;
					}
				}
				Javelin.recruit(candidate);
				break;
			}
			if (before == Squad.active.size()) {
				return false;
			}
		} else if (o.equals(RESSURECT)) {
			if (EndBattle.lastkilled == null) {
				text += "\nNo ally has died yet.";
				Javelin.app.switchScreen(this);
				return false;
			}
			Haxor.singleton.options.remove(RESSURECT);
			EndBattle.lastkilled.hp = EndBattle.lastkilled.maxhp;
			Squad.active.members.add(EndBattle.lastkilled);
		} else if (o.equals(GENERATEDUNGEON)) {
			generate(new Dungeon());
		} else if (o.equals(GENERATELAIR)) {
			generate(new Lair());
		} else if (o.equals(REBIRTH)) {
			ChallengeRatingCalculator.calculateCr(target.source);
			Float originalcr = target.source.challengeRating;
			target.spells.clear();
			String customname = target.source.customName;
			target.source = Summon.findmonster(target.source.name);
			target.source.customName = customname;
			ChallengeRatingCalculator.calculateCr(target.source);
			target.xp = target.xp.add(
					new BigDecimal(originalcr - target.source.challengeRating));
			if (target.xp.intValue() < 0) {
				target.xp = new BigDecimal(0);
			}
		} else if (o.equals(WIN)) {
			for (Squad s : Squad.squads) {
				WorldScreen.lastday *= 10;
				Squad.active.hourselapsed =
						Math.round(WorldScreen.lastday * 24);
			}
			for (String win : WINMESSAGES) {
				text = win;
				text += "\nPress any key to continue...";
				print();
			}
			text = "Do you want to finish the current game? Press y for yes, n for no.";
			Character input = print();
			while (input != 'y' && input != 'n') {
				input = print();
			}
			if (input == 'y') {
				StateManager.clear();
				text = "Congratulations!\n\n" + Javelin.record()
						+ "\n\nThank you for playing :) press ENTER to leave...";
				input = print();
				while (input != '\n') {
					input = print();
				}
				System.exit(0);
			}
		} else if (o.equals(REMOVEABILITIY)) {
			if (!removeability(target)) {
				return true;
			}
		}
		Haxor.singleton.tickets -= o.price;
		StateManager.save();
		return true;
	}

	public Character print() {
		Javelin.app.switchScreen(this);
		Character feedback = feedback();
		return feedback;
	}

	boolean removeability(Combatant target) {
		text = "";
		refresh();
		ArrayList<String> types = new ArrayList<String>();
		listremovals(target, types);
		if (types.isEmpty()) {
			text = "Unit has no abilities that can be removed.";
			refresh();
			getInput();
			return false;// abort
		}
		int i = CastSpells.choose("Which type of ability? Press q to quit.",
				types, true, false);
		if (i < 0 || i >= types.size()) {
			return false;// abort
		}
		String type = types.get(i);
		if (type == "Breath") {
			target.source.breaths.remove(CastSpells.choose("Select a breath:",
					target.source.breaths, true, true));
		} else if (type == "Spell") {
			target.spells.remove(CastSpells.choose("Select a spell:",
					target.spells, true, true));
		} else if (type == "Mêléé attack") {
			removeaattack(target.source.melee);
		} else if (type == "Ranged attack") {
			removeaattack(target.source.ranged);
		}
		return true;
	}

	void removeaattack(ArrayList<AttackSequence> attacksp) {
		List<AttackSequence> removable = selectattacks(attacksp);
		attacksp.remove(removable.get(CastSpells.choose("Remove which attack?",
				removable, true, true)));
	}

	void listremovals(Combatant target, ArrayList<String> types) {
		if (!target.source.breaths.isEmpty()) {
			types.add("Breath");
		}
		if (!selectattacks(target.source.melee).isEmpty()) {
			types.add("Mêléé attack");
		}
		if (!selectattacks(target.source.ranged).isEmpty()) {
			types.add("Ranged attack");
		}
		if (!target.spells.isEmpty()) {
			types.add("Spell");
		}
	}

	List<AttackSequence> selectattacks(ArrayList<AttackSequence> attacksp) {
		ArrayList<AttackSequence> attacks =
				new ArrayList<AttackSequence>(attacksp);
		for (AttackSequence attack : attacksp) {
			if (attack.powerful || attack.rapid) {
				attacks.remove(attack);
			}
		}
		if (attacks.size() == 1) {
			attacks.clear();
		}
		return attacks;
	}

	void generate(WorldPlace place) {
		int x = Haxor.singleton.x;
		int y = Haxor.singleton.y;
		while (x == Haxor.singleton.x && y == Haxor.singleton.y
				|| WorldScreen.getactor(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || x >= WorldMap.MAPDIMENSION || y < 0
					|| y >= WorldMap.MAPDIMENSION) {
				generate(place);
				return;
			}
		}
		place.x = x;
		place.y = y;
		place.place();
	}

	@Override
	public String printInfo() {
		return DAILYQUOTE + "\nYou have " + Haxor.singleton.tickets
				+ " tickets.";
	}

	@Override
	public List<Option> getOptions() {
		ArrayList<Option> options = new ArrayList<Option>();
		options.add(CHANGEAVATAR);
		options.add(REMOVEABILITIY);
		options.add(GENERATEDUNGEON);
		options.add(GENERATELAIR);
		options.add(REBIRTH);
		options.add(WIN);
		options.addAll(Haxor.singleton.options);
		return options;
	}

	@Override
	public String getCurrency() {
		return "";
	}

	@Override
	public String printpriceinfo(Option o) {
		long price = Math.round(o.price);

		return " (" + price + " " + (price == 1 ? "ticket" : "tickets") + ")";
	}
}