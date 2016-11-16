package javelin.view.screen.town.option;

import javelin.model.unit.Monster;
import javelin.view.screen.Option;

/***
 * 
 * @see RecruitScreen
 * @author alex
 */
public class RecruitOption extends Option {
	/** Unit type to recruited. */
	public Monster m;

	/** Constructor. */
	public RecruitOption(String name, double d, Monster m) {
		super(name, d);
		this.m = m;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof RecruitOption) {
			final RecruitOption ro = (RecruitOption) obj;
			return m.name.equals(ro.m.name);
		}
		return false;
	}
}
