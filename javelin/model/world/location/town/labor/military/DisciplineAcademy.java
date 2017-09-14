package javelin.model.world.location.town.labor.military;

import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Calendar;
import javelin.controller.challenge.ChallengeRatingCalculator;
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
			for (Combatant c : new Combatant[] { initiate, disciple, master }) {
				if (c != null) {
					options.add(new HireOption(c));
				}
			}
			return options;
		}

		@Override
		public boolean select(Option op) {
			HireOption h = op instanceof HireOption ? (HireOption) op : null;
			if (h != null) {
				if (!MercenariesGuild.recruit(h.c, false)) {
					final String error = "You don't have enough money to pay today's advance!\n"
							+ "Press any key to continue...";
					String text = this.text;
					print(text + "\n" + error);
					feedback();
					this.text = text;
					return false;
				} else if (h.c == initiate) {
					initiate = null;
				} else if (h.c == disciple) {
					disciple = null;
				} else if (h.c == master) {
					master = null;
				}
				return true;
			}
			return super.select(op);
		}

		@Override
		protected boolean upgrade(UpgradeOption o, Combatant c) {
			FeatUpgrade fu = o.u instanceof FeatUpgrade ? (FeatUpgrade) o.u
					: null;
			MartialTraining mt = fu != null
					&& fu.feat instanceof MartialTraining
							? (MartialTraining) fu.feat : null;
			if (mt != null) {
				float cr = ChallengeRatingCalculator
						.calculaterawcr(c.source)[1];
				train(c, c.xp.floatValue(), cr);
				return ChallengeRatingCalculator
						.calculaterawcr(c.source)[1] > cr;
			}
			return super.upgrade(o, c);
		}
	}

	/** CR 5 mercenary. */
	Combatant initiate = null;
	/** CR 10 mercenary. */
	Combatant disciple = null;
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
		initiate = train(initiate, 1, 5);
		disciple = train(disciple, Javelin.DEBUG ? 1 : 2, 10);
		master = train(master, Javelin.DEBUG ? 1 : 4, 15);
	}

	@Override
	public List<Combatant> getcombatants() {
		List<Combatant> combatants = super.getcombatants();
		for (Combatant c : new Combatant[] { initiate, disciple, master }) {
			if (c != null) {
				combatants.add(c);
			}
		}
		return combatants;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		initiate = train(initiate, Calendar.MONTH, 5);
		disciple = train(disciple, Calendar.SEASON, 10);
		master = train(master, Calendar.YEAR, 15);
	}

	Combatant train(Combatant rank, int period, int level) {
		if (rank != null || !RPG.chancein(period)) {
			return rank;
		}
		Combatant c = new Combatant(RPG.pick(TrainingHall.CANDIDATES), true);
		c.setmercenary(true);
		train(c, level, ChallengeRatingCalculator.calculaterawcr(c.source)[1]);
		c.postupgradeautomatic(true, d.classupgrade);
		name(c);
		return c;
	}

	void name(Combatant c) {
		c.source.customName = d.name + " initiate";
		for (Feat f : c.source.feats) {
			MartialTraining mt = f instanceof MartialTraining
					? (MartialTraining) f : null;
			if (mt != null) {
				c.source.customName = d.name + " " + mt.getrank().toLowerCase();
				return;
			}
		}
	}

	Combatant train(Combatant c, float xp, float cr) {
		for (Upgrade u : d.getupgrades()) {
			Combatant c2 = c.clone().clonesource();
			if (!u.upgrade(c2)) {
				continue;
			}
			if (ChallengeRatingCalculator.calculaterawcr(c2.source)[1]
					- cr > xp) {
				continue;
			}
			c.source = c.source.clone();
			u.upgrade(c);
			return train(c, xp, cr);
		}
		return c;
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
