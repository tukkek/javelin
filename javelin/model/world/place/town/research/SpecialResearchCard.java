package javelin.model.world.place.town.research;

import javelin.model.world.place.town.ResearchData;
import javelin.model.world.place.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.ResearchScreen;

/**
 * These are supposed to be a stack of research cards each with a unique effect.
 * 
 * They have #aiable as <code>false</code> by default. And sometimes make use of
 * #immediate as well.
 * 
 * @author alex
 */
public abstract class SpecialResearchCard extends Research {

	/**
	 * @param t
	 *            Town to be validated.
	 * @param s
	 *            Used to print an error message if <code>false</code> is
	 *            returned.
	 * @return <code>true</code> if there is at least 1 card in the
	 *         {@link ResearchData#hand} that can be discarded.
	 */
	protected static boolean candiscard(Town t, ResearchScreen s) {
		for (int i =
				ResearchData.NATIVEUPGRADE; i <= ResearchData.MONSTERLAIR; i++) {
			if (t.research.hand[i] != null) {
				return true;
			}
		}
		s.print(s.text
				+ "\nThere are no more options for you to discard.\nPress any key to continue...");
		IntroScreen.feedback();
		return false;
	}

	public SpecialResearchCard(String name, double price) {
		super(name, price);
		aiable = false;
	}

	@Override
	public boolean isrepeated(Town t) {
		return false;
	}
}
