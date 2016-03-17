package javelin.model.world.town.research;

import java.util.List;

import javelin.model.world.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;

/**
 * For 1 labor allows player to discard one option from
 * {@link Town#researchhand}.
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
		List<Option> options = s.getOptions();
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
		for (int i = 0; i < t.researchhand.length; i++) {
			if (t.researchhand[i] == choice) {
				t.researchhand[i] = null;
				return;
			}
		}
	}
}
