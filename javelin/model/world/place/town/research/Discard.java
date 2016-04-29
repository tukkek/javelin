package javelin.model.world.place.town.research;

import java.util.List;

import javelin.model.world.place.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.ResearchScreen;

/**
 * For 1 labor allows player to discard one option from
 * {@link Town#research.researchhand}.
 * 
 * @author alex
 */
public class Discard extends SpecialResearchCard {

	public Discard() {
		super("Discard choice", 1);
		immediate = true;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		List<Option> options = s.getoptions();
		s.print(s.text + "Discard which research? Select from 2 to "
				+ (options.size() - 1) + ".");
		Option choice = null;
		while (choice == null) {
			try {
				choice = options.get(Integer.parseInt(
						Character.toString(IntroScreen.feedback())) - 1);
			} catch (NumberFormatException e) {
				continue;
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
		for (int i = 0; i < t.research.hand.length; i++) {
			if (t.research.hand[i] == choice) {
				t.research.hand[i] = null;
				return;
			}
		}
	}
}
