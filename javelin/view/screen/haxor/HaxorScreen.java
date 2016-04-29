package javelin.view.screen.haxor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.action.world.CastSpells;
import javelin.controller.db.StateManager;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
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
	 * collections of upgrades and items that eventually got too big to be
	 * purely random (or it would be a pain in the ass to keep track of what is
	 * where in each game) - but in the end is there anything more win than
	 * Helloween? So I had to make the best out of this coincidence :)
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

	/** See {@link SelectScreen#SelectScreen(String, Town)}. */
	public HaxorScreen(String name, Town t) {
		super(name, t);
	}

	@Override
	public boolean select(Option o) {
		double price = o.price;
		if (price > Haxor.singleton.tickets) {
			text += "\nNot enough tickets...";
			Javelin.app.switchScreen(this);
			return false;
		}
		if (price == 0) {// remove booster
			if (o instanceof ChangeAvatar) {
				o.price = 1;
			} else {
				for (Option booster : new ArrayList<Option>(
						Haxor.singleton.options)) {
					if (booster.price == 0
							&& !(booster instanceof ChangeAvatar)) {
						Haxor.singleton.options.remove(booster);
					}
				}
			}
		}
		Hax h = (Hax) o;
		if (!h.hack(h.requirestarget ? selectmember() : null, this)) {
			return false;
		}
		Haxor.singleton.tickets -= price;
		StateManager.save();
		return true;
	}

	protected Combatant selectmember() {
		Combatant target;
		ArrayList<String> members = new ArrayList<String>();
		for (Combatant c : Squad.active.members) {
			members.add(c.toString());
		}
		target = Squad.active.members.get(CastSpells
				.choose("Select a squad member:", members, true, true));
		return target;
	}

	Character print() {
		Javelin.app.switchScreen(this);
		return feedback();
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

	void generate(WorldActor place) {
		int x = Haxor.singleton.x;
		int y = Haxor.singleton.y;
		while (x == Haxor.singleton.x && y == Haxor.singleton.y
				|| WorldScreen.getactor(x, y) != null) {
			x += RPG.pick(new int[] { -1, 0, +1 });
			y += RPG.pick(new int[] { -1, 0, +1 });
			if (x < 0 || x >= World.MAPDIMENSION || y < 0
					|| y >= World.MAPDIMENSION) {
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
	public List<Option> getoptions() {
		return Haxor.singleton.options;
	}

	@Override
	public String getCurrency() {
		return "";
	}

	@Override
	public String printpriceinfo(Option o) {
		if (o.price == 0) {
			return o instanceof ChangeAvatar ? (" (free)")
					: " (free, single use)";
		}
		long price = Math.round(o.price);
		return " (" + price + " " + (price == 1 ? "ticket" : "tickets") + ")";
	}
}