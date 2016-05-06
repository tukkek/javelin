package javelin.model.world.place.town.research;

import java.util.List;

import javelin.model.world.place.town.ResearchData;
import javelin.model.world.place.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.ResearchScreen;

/**
 * For 1 labor allows player to discard one option from {@link Town#research
 * .researchhand}.
 * 
 * @author alex
 */
public class Discard extends SpecialResearchCard {

	static final double COST = 1;

	/** Constructor. */
	public Discard() {
		super("Discard choice", COST);
		immediate = true;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		if (!candiscard(t, s)) {
			t.labor += COST;
			return;
		}
		Integer mini = Integer.MAX_VALUE;
		Integer maxi = Integer.MIN_VALUE;
		List<Option> options = s.getoptions();
		for (int i =
				ResearchData.NATIVEUPGRADE; i <= ResearchData.MONSTERLAIR; i++) {
			if (t.research.hand[i] != null) {
				if (i < mini) {
					mini = i;
				}
				if (i > maxi) {
					maxi = i;
				}
			}
		}
		s.print(s.text + "Discard which research? Select from "
				+ (options.indexOf(t.research.hand[mini]) + 1) + " to "
				+ (options.indexOf(t.research.hand[maxi]) + 1) + ".");
		Option choice = choose(mini, maxi, options, t.research.hand);
		for (int i = 0; i < t.research.hand.length; i++) {
			if (t.research.hand[i] == choice) {
				t.research.hand[i] = null;
				return;
			}
		}
	}

	Option choose(Integer mini, Integer maxi, List<Option> options,
			Research[] hand) {
		Option choice = null;
		while (choice == null) {
			try {
				int i = indexof(hand,
						options.get(Integer
								.parseInt(Character
										.toString(IntroScreen.feedback()))
								- 1));
				if (!(mini <= i && i <= maxi)) {
					continue;
				}
				choice = hand[i];
			} catch (NumberFormatException e) {
				continue;
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
		return choice;
	}

	private int indexof(Research[] hand, Option option) {
		for (int i = 0; i < hand.length; i++) {
			if (hand[i] == option) {
				return i;
			}
		}
		throw new RuntimeException("no index for " + option + " #discard");
	}
}
