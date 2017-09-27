package javelin.model.world.location.town.labor.military;

import java.util.Collections;
import java.util.List;

import javelin.controller.Calendar;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.MartialTraining;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.PurchaseScreen;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

/**
 * TODO How to offer a defined upgrade path so that it doesn't become to
 * burdersome for players to learn? Also for upgrading NPCs? Here it should be
 * easy enough to have a {@link ScreenOption} that takes care of that but what
 * for NPCs?
 * 
 * @author alex
 */
public class DisciplineAcademy extends Academy {
	static final int LEVERSTUDENT = 9;
	static final int LEVELTEACHER = 12;
	static final int LEVERMASTER = 16;

	public class HireOption extends Option {
		Combatant c;

		public HireOption(Combatant c) {
			super("Hire " + c + " ($"
					+ PurchaseScreen.formatcost(MercenariesGuild.getfee(c))
					+ "/day)", 0);
			this.c = c;
		}
	}

	public class DisciplineAcademyScreen extends AcademyScreen {
		public DisciplineAcademyScreen(Academy academy) {
			super(academy, null);
		}

		@Override
		public List<Option> getoptions() {
			List<Option> options = super.getoptions();
			for (Combatant c : new Combatant[] { student, teacher, master }) {
				if (c != null) {
					options.add(new HireOption(c));
				}
			}
			return options;
		}

		@Override
		public boolean select(Option op) {
			HireOption hire = op instanceof HireOption ? (HireOption) op : null;
			if (hire != null) {
				if (!MercenariesGuild.recruit(hire.c, false)) {
					final String error = "You don't have enough money to pay today's advance!\n"
							+ "Press any key to continue...";
					printmessage(error);
					return false;
				} else if (hire.c == student) {
					student = null;
				} else if (hire.c == teacher) {
					teacher = null;
				} else if (hire.c == master) {
					master = null;
				}
				return true;
			}
			return super.select(op);
		}

		@Override
		protected boolean upgrade(UpgradeOption o, Combatant c) {
			MartialTraining mt = getmartialtrainingfeat(o);
			if (mt != null) {
				float cr = CrCalculator.calculaterawcr(c.source)[1];
				train(c, c.xp.floatValue(), cr, d.getupgrades());
				return CrCalculator.calculaterawcr(c.source)[1] > cr;
			}
			return super.upgrade(o, c);
		}

		MartialTraining getmartialtrainingfeat(UpgradeOption o) {
			if (!(o.u instanceof FeatUpgrade)) {
				return null;
			}
			final FeatUpgrade fu = (FeatUpgrade) o.u;
			return fu.feat instanceof MartialTraining
					? (MartialTraining) fu.feat : null;
		}
	}

	/** CR 5 mercenary. */
	Combatant student = null;
	/** CR 10 mercenary. */
	Combatant teacher = null;
	/** CR 15 mercenary. */
	Combatant master = null;
	Discipline d;

	public DisciplineAcademy(Discipline d) {
		super(d.name + " academy", null, 5, 15, Collections.EMPTY_SET,
				d.abilityupgrade, d.classupgrade);
		this.d = d;
		descriptionunknown = descriptionknown;
		upgrades.add(d.skillupgrade);
		upgrades.add(d.knowledgeupgrade);
		upgrades.add(d.trainingupgrade);
		student = train(student, LEVERSTUDENT, 1);
		teacher = train(teacher, LEVELTEACHER, 2);
		master = train(master, LEVERMASTER, 4);
	}

	@Override
	public List<Combatant> getcombatants() {
		List<Combatant> combatants = super.getcombatants();
		for (Combatant c : new Combatant[] { student, teacher, master }) {
			if (c != null) {
				combatants.add(c);
			}
		}
		return combatants;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		student = train(student, LEVERSTUDENT, Calendar.MONTH);
		teacher = train(teacher, LEVELTEACHER, Calendar.SEASON);
		master = train(master, LEVERMASTER, Calendar.YEAR);
	}

	Combatant train(Combatant c, int level, int period) {
		if (c != null || !RPG.chancein(period)) {
			return c;
		}
		c = new Combatant(RPG.pick(TrainingHall.CANDIDATES), true);
		c.setmercenary(true);
		train(c, level, CrCalculator.calculaterawcr(c.source)[1],
				d.getupgrades());
		c.postupgradeautomatic(d.classupgrade);
		name(c);
		return c;
	}

	void name(Combatant c) {
		c.source.customName = d.name + " initiate";
		for (Feat f : c.source.feats) {
			if (f instanceof MartialTraining) {
				final MartialTraining mt = (MartialTraining) f;
				c.source.customName = d.name + " " + mt.getrank().toLowerCase();
				return;
			}
		}
	}

	/**
	 * @param c
	 *            Train a combatant...
	 * @param xp
	 *            ... up to this amount of XP (not removed form
	 *            {@link Combatant#xp}) ...
	 * @param cr
	 *            ... and this amount of raw CR (with golden rule applied)...
	 * @param upgrades
	 *            in these Upgrades...
	 * @see CrCalculator#calculaterawcr(javelin.model.unit.Monster)
	 */
	public static void train(Combatant c, float xp, float cr,
			Upgrade[] upgrades) {
		for (Upgrade u : upgrades) {
			Combatant c2 = c.clone().clonesource();
			if (!u.upgrade(c2)) {
				continue;
			}
			final float newcr = CrCalculator.calculaterawcr(c2.source)[1];
			if (newcr - cr > xp) {
				continue;
			}
			c.source = c.source.clone();
			u.upgrade(c);
			train(c, xp, cr, upgrades);
			return;
		}
	}

	@Override
	public int getlabor() {
		return 10;
	}

	@Override
	protected AcademyScreen getscreen() {
		return new DisciplineAcademyScreen(this);
	}
}