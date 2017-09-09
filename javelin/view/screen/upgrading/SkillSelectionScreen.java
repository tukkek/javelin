package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.SkillsFactor;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.skill.SkillUpgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * Used when certain upgrades require the unit to also advance in {@link Skills}
 * . This is necessary because otherwise the {@link SkillsFactor} calculation
 * would make these upgrades create an negative net-gain and in thie case they
 * would not be applied by the {@link UpgradingScreen}.
 *
 * @author alex
 */
public class SkillSelectionScreen extends SelectScreen {
	static final int TAB = 20;

	class SkillOption extends Option {
		SkillUpgrade u;

		SkillOption(SkillUpgrade u) {
			super(u.skillname, 0);
			name += ":";
			while (name.length() < TAB) {
				name += " ";
			}
			int ranks = u.gettotal(m);
			if (ranks < Math.abs(10)) {
				name += " ";
			}
			if (ranks >= 0) {
				name += "+";
			}
			name += ranks;
			this.u = u;
		}
	}

	List<SkillUpgrade> classskills = new ArrayList<SkillUpgrade>();
	Monster m;
	boolean dontbother;

	/**
	 * @param m
	 *            Monster that needs to spend skill points.
	 * @param u
	 *            If this is a Class
	 */
	public SkillSelectionScreen(Monster m, Upgrade u) {
		super("Select the skills " + m + " will train:", null);
		showquit = false;
		this.m = m;
		dontbother = !canspend(m);
		if (u != null && u instanceof ClassLevelUpgrade) {
			addclassskills((ClassLevelUpgrade) u);
		} else {
			ClassLevelUpgrade.init();
			for (ClassLevelUpgrade c : ClassLevelUpgrade.classes) {
				if (c.getlevel(m) > 0) {
					addclassskills(c);
				}
			}
		}
	}

	private void addclassskills(ClassLevelUpgrade c) {
		for (SkillUpgrade u : c.classskills) {
			classskills.add(u);
		}
	}

	@Override
	public String getCurrency() {
		return "";
	}

	@Override
	public String printinfo() {
		return "You have " + m.skillpool + " points left to distribute\n"//
				+ "Cross-class skills (X) cost 2 points to upgrade\n" //
				+ "Press ENTER to spend your remaining points automatically\n"
				+ "Press q to discard your remaining points\n";
	}

	@Override
	public String printpriceinfo(Option o) {
		SkillOption s = (SkillOption) o;
		if (ismaxed(s.u)) {
			return " (max)";
		}
		if (!classskills.contains(s.u)) {
			return " X";
		}
		return "";
	}

	boolean ismaxed(SkillUpgrade s) {
		return s.getranks(m.skills) >= m.hd.count();
	}

	@Override
	public boolean select(Option o) {
		SkillOption s = (SkillOption) o;
		return !ismaxed(s.u) && buy(s.u);
	}

	boolean buy(SkillUpgrade s) {
		int cost = classskills.contains(s) ? 1 : 2;
		if (cost > m.skillpool) {
			return false;
		}
		m.skillpool -= cost;
		s.setranks(m.skills, s.getranks(m.skills) + 1);
		return true;
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> skills = new ArrayList<Option>();
		for (SkillUpgrade u : SkillUpgrade.ALL) {
			skills.add(new SkillOption(u));
		}
		return skills;
	}

	@Override
	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				SkillOption a = (SkillOption) o1;
				SkillOption b = (SkillOption) o2;
				return a.u.skillname.compareTo(b.u.skillname);
			}
		};
	}

	@Override
	protected boolean select(char feedback, List<Option> options) {
		if (feedback == '\n') {
			upgradeautomatically();
			stayopen = false;
			return true;
		}
		return super.select(feedback, options);
	}

	@Override
	public void onexit() {
		m.skillpool = 0;
		ChallengeRatingCalculator.calculatecr(m);
	}

	/**
	 * Can be called instead of {@link #show()} to spend the points
	 * automatically.
	 */
	public void upgradeautomatically() {
		if (!canspend(m)) {
			return;
		}
		spend(classskills);
		ArrayList<SkillUpgrade> crossclass = new ArrayList<SkillUpgrade>();
		for (SkillUpgrade s : SkillUpgrade.ALL) {
			if (!classskills.contains(s)) {
				crossclass.add(s);
			}
		}
		spend(crossclass);
	}

	void spend(List<SkillUpgrade> skills) {
		skills = new ArrayList<SkillUpgrade>(skills);
		Collections.shuffle(skills);
		for (SkillUpgrade s : skills) {
			if (m.skillpool == 0) {
				return;
			}
			if (ismaxed(s)) {
				continue;
			}
			if (!buy(s)) {
				return;
			}
		}
	}

	@Override
	public void show() {
		if (dontbother) {
			return;
		}
		super.show();
	}

	/**
	 * @return <code>true</code> if has a positive number of
	 *         {@link Monster#skillpool} to spend.
	 */
	static public boolean canspend(Monster m) {
		return m.skillpool > 0;
	}
}
