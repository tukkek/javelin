package javelin.view.screen.wish;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.key.TempleKey;
import javelin.model.item.relic.Relic;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * If you conquer 7 {@link TempleKey}s, unlock their challenges and survive you can
 * "win the game". It will actually just multiply your scores by 10 and ask if
 * you want to conclude the game.
 * 
 * @author alex
 */
public class Win extends Wish {
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
	static final String[] WINMESSAGES = new String[] {
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
			"You're the Keeper of the Seven Keys\nYou locked up the seven seas\nAnd the Seer of Visions can now rest in peace\nThere ain't no more demons and no more disease\nAnd, mankind, live up, you're free again\nYes the tyrant is dead, he is gone, overthrown\nYou have given our souls back to light\n\n    \"Keeper of the seven keys\", by the german power metal band Helloween\n", };

	/** Constructor. */
	public Win(String name, Character keyp, double price,
			boolean requirestargetp, WishScreen s) {
		super(name, keyp, price, requirestargetp, s);
	}

	@Override
	protected boolean wish(Combatant target) {
		for (Combatant c : Squad.active.members) {
			ArrayList<Item> bag = Squad.active.equipment.get(c.id);
			for (Item i : new ArrayList<Item>(bag)) {
				if (i instanceof Relic) {
					bag.remove(i);
				}
			}
		}
		for (Actor a : World.getactors()) {
			if (a instanceof Temple) {
				a.remove();
			}
		}
		for (Squad squad : Squad.getsquads()) {
			WorldScreen.lastday *= 10;
			squad.hourselapsed = Math.round(WorldScreen.lastday * 24);
		}
		for (String win : Win.WINMESSAGES) {
			screen.text = win;
			screen.text += "\nPress any key to continue...\n\n";
			screen.print();
		}
		int current = Math.round(Math.round(WorldScreen.lastday));
		screen.text += "Your highscore record is " + Javelin.gethighscore()
				+ "\n";
		screen.text += "Your current score now is " + current + "\n";
		screen.text += "\nDo you want to finish the current game? Press y for yes, n to continue playing.";
		Character input = screen.print();
		while (input != 'y' && input != 'n') {
			input = screen.print();
		}
		if (input == 'y') {
			StateManager.clear();
			screen.text = "Congratulations!\n\n" + Javelin.record()
					+ "\n\nThank you for playing :) press ENTER to leave...";
			input = screen.print();
			while (input != '\n') {
				input = screen.print();
			}
			System.exit(0);
		}
		return true;
	}

	@Override
	public String validate() {
		if (screen.rubies < price || Javelin.DEBUG) {
			/* let the player think he only needs rubies to win the game hihi */
			return null;
		}
		int nrelics = 0;
		for (Combatant c : Squad.active.members) {
			for (Item i : Squad.active.equipment.get(c.id)) {
				if (i instanceof Relic) {
					nrelics += 1;
				}
			}
		}
		int all = Realm.values().length;
		return nrelics == all ? null
				: "You need to bring the " + all + " relics here first!";
	}
}