package javelin.view.screen.town.option;

import javelin.model.world.place.town.Town;
import javelin.view.screen.town.ResearchScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Opens up a {@link ResearchScreen}.
 * 
 * @author alex
 */
public class ResearchScreenOption extends ScreenOption {
	Town t;

	public ResearchScreenOption(Town town) {
		super("Research", town, 'R');
		t = town;
	}

	@Override
	public SelectScreen show() {
		t.research.queue.clear();
		return new ResearchScreen(name, t);
	}
}