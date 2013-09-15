package javelin.view.screen.town.option;

import javelin.model.unit.Monster;

public class RecruitOption extends Option {
	public Monster m;

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
