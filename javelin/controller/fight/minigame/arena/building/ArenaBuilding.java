package javelin.controller.fight.minigame.arena.building;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * TODO on upgrade start fast healing
 *
 * @author alex
 */
public abstract class ArenaBuilding extends Combatant {
	static BuildingLevel[] LEVELS = new BuildingLevel[] {
			new BuildingLevel(0, 5, 70, 60, 5, 0),
			new BuildingLevel(1, 10, 110, 90, 7, 7500 * ArenaFight.BOOST),
			new BuildingLevel(2, 15, 240, 180, 8, 25000 * ArenaFight.BOOST),
			new BuildingLevel(3, 20, 600, 540, 8, 60000 * ArenaFight.BOOST), };

	public static class BuildingLevel {
		int level;
		int repair;
		int hp;
		int damagethresold;
		int hardness;
		int cost;

		public BuildingLevel(int level, int repair, int hp, int damagethresold,
				int hardness, float cost) {
			super();
			this.level = level;
			this.repair = repair;
			this.hp = hp;
			this.damagethresold = damagethresold;
			this.hardness = hardness;
			this.cost = Math.round(cost);
		}
	}

	protected class BuildingUpgradeOption extends Option {
		protected BuildingUpgradeOption() {
			super("Upgrade " + source.customName.toLowerCase(),
					getupgradecost(), 'u');
		}

		protected void upgrade() {
			materials = getupgradecost();
			setlevel(ArenaBuilding.LEVELS[level + 1]);
			repairing = true;
			upgradebuilding();
		}

		public boolean buy(InfoScreen s) {
			int gold = ArenaFight.get().gold;
			if (gold >= price) {
				ArenaFight.get().gold -= price;
				upgrade();
				return true;
			}
			s.print("Not enough gold (you currently have $"
					+ SelectScreen.formatcost(gold)
					+ ").\n\nPress any key to continue....");
			return false;
		}
	}

	final protected String actiondescription;

	int materials = 0;
	/** Building level from 0 to 4. */
	int level;
	int damagethresold;
	public boolean repairing = false;

	public ArenaBuilding(String name, String avatar, String description) {
		super(Javelin.getmonster("Building"), false);
		actiondescription = description;
		source.customName = name;
		source.passive = true;
		source.avatarfile = avatar;
		source.immunitytocritical = true;
		source.immunitytomind = true;
		source.immunitytoparalysis = true;
		source.immunitytopoison = true;
		setlevel(ArenaBuilding.LEVELS[0]);
		hp = maxhp;
	}

	void setlevel(ArenaBuilding.BuildingLevel level) {
		this.level = level.level;
		maxhp = level.hp;
		damagethresold = level.damagethresold;
		source.dr = level.hardness;
		source.challengerating = (level.level + 1) * 5f;
	}

	/** TODO use */
	Integer getupgradecost() {
		int next = level + 1;
		return next < ArenaBuilding.LEVELS.length
				? getrepaircost() + ArenaBuilding.LEVELS[next].cost : null;
	}

	@Override
	public void act(BattleState s) {
		ArenaBuilding c = (ArenaBuilding) s.clone(this);
		c.ap += 1;
		if (repairing) {
			int repair = LEVELS[c.level].repair;
			c.materials -= repair * c.source.dr;
			c.hp = Math.min(c.hp + repair, c.maxhp);
			if (c.hp == c.maxhp || c.materials <= 0) {
				c.repairing = false;
			}
		}
	}

	abstract protected void upgradebuilding();

	@Override
	public BattleMouseAction getmouseaction() {
		return new BattleMouseAction() {

			@Override
			public void onenter(Combatant current, Combatant target, Tile t,
					BattleState s) {
				Game.message(isdamaged() ? getrepairmessage()
						: getactiondescription(current), Delay.NONE);
			}

			@Override
			public boolean determine(Combatant current, Combatant target,
					BattleState s) {
				return true;
			}

			@Override
			public void act(final Combatant current, final Combatant target,
					final BattleState s) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						if (!current.isadjacent(target)) {
							Game.messagepanel.clear();
							Game.message("Too far away...", Delay.WAIT);
						} else if (isdamaged()) {
							if (repair()) {
								Game.messagepanel.clear();
								String underrepair = source.customName
										+ " is being repaired...";
								Game.message(underrepair, Delay.WAIT);
								Fight.state.next.ap += 1;
							} else {
								Game.messagepanel.clear();
								Game.message("Not enough gold...", Delay.WAIT);
							}
						} else {
							if (click(current)) {
								s.clone(current).ap += 1;
							}
							Javelin.app.switchScreen(BattleScreen.active);
						}
					}
				});
			}
		};

	}

	boolean repair() {
		if (repairing) {
			return true;
		}
		int cost = getrepaircost();
		if (ArenaFight.get().gold < cost) {
			return false;
		}
		ArenaFight.get().gold -= cost;
		repairing = true;
		materials += cost;
		return true;
	}

	String getrepairmessage() {
		String suffix = "\n\nYou currently have $"
				+ SelectScreen.formatcost(ArenaFight.get().gold) + ".";
		String name = source.customName.toLowerCase();
		if (repairing) {
			return "This " + name + " is being repaired." + suffix;
		}
		return "This " + name + " needs to be repaired ($"
				+ SelectScreen.formatcost(getrepaircost())
				+ "). Click to start repairs." + suffix;
	}

	int getrepaircost() {
		return Math.max(1, (maxhp - hp) * source.dr - materials);
	}

	public boolean isdamaged() {
		return hp <= damagethresold || repairing;
	}

	abstract protected boolean click(Combatant current);

	public String getactiondescription(Combatant current) {
		return actiondescription;
	}

	@Override
	public String getstatus() {
		switch (getnumericstatus()) {
		case STATUSUNHARMED:
			return "pristine";
		case STATUSSCRATCHED:
			return "scathed";
		case STATUSHURT:
			return "worn";
		case STATUSWOUNDED:
			return "broken";
		case STATUSINJURED:
			return "torn";
		case STATUSDYING:
			return "demolished";
		case STATUSUNCONSCIOUS:
		case STATUSDEAD:
			return "destroyed";
		default:
			throw new RuntimeException(
					"Unknown possibility: " + getnumericstatus());
		}
	}

	@Override
	public boolean ispenalized(BattleState s) {
		return isdamaged();
	}

	@Override
	public void damage(int damagep, BattleState s, int reduce) {
		super.damage(damagep, s, reduce);
		if (repairing) {
			ArenaBuilding b = (ArenaBuilding) s.clone(this);
			if (b != null) {
				b.repairing = false;
			}
		}
	}
}