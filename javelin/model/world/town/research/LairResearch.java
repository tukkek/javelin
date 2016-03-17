package javelin.model.world.town.research;

import javelin.model.unit.Monster;
import javelin.model.world.town.Town;
import javelin.view.screen.town.RecruitScreen;
import javelin.view.screen.town.option.RecruitOption;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;

/**
 * Allows a new {@link Monster} to be recruited in this town.
 * 
 * @see RecruitScreen
 * @author alex
 */
public class LairResearch extends Research {

	private Monster m;

	public LairResearch(Monster m) {
		super(m.toString(), m.challengeRating);
		this.m = m;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.lairs.add(
				new RecruitOption(m.toString(), m.challengeRating * 100, m));
	}

	@Override
	protected boolean isrepeated(Town t) {
		for (RecruitOption r : t.lairs) {
			if (r.m.equals(m)) {
				return true;
			}
		}
		return false;
	}

}
