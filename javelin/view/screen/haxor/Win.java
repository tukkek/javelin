package javelin.view.screen.haxor;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.view.screen.WorldScreen;

/**
 * If you conquer 7 {@link Key}s, unlock their challenges and survive you can
 * "win the game". It will actually just multiply your scores by 10 and ask if
 * you want to conclude the game.
 * 
 * @author alex
 */
public class Win extends Hax {
	public Win(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		for (WorldActor a : Squad.getall(Squad.class)) {
			WorldScreen.lastday *= 10;
			((Squad) a).hourselapsed = Math.round(WorldScreen.lastday * 24);
		}
		for (String win : s.WINMESSAGES) {
			s.text = win;
			s.text += "\nPress any key to continue...";
			s.print();
		}
		s.text += "Your highscore record is " + Javelin.gethighscore() + "\n";
		s.text += "Your current score now is " + WorldScreen.lastday + "\n";
		s.text +=
				"\nDo you want to finish the current game? Press y for yes, n for no.";
		Character input = s.print();
		while (input != 'y' && input != 'n') {
			input = s.print();
		}
		if (input == 'y') {
			StateManager.clear();
			s.text = "Congratulations!\n\n" + Javelin.record()
					+ "\n\nThank you for playing :) press ENTER to leave...";
			input = s.print();
			while (input != '\n') {
				input = s.print();
			}
			System.exit(0);
		}
		return true;
	}
}