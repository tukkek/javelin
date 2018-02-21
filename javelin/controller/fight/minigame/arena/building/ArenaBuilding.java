package javelin.controller.fight.minigame.arena.building;

import javelin.Javelin;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;

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
				int hardness, int cost) {
			super();
			this.level = level;
			this.repair = repair;
			this.hp = hp;
			this.damagethresold = damagethresold;
			this.hardness = hardness;
			this.cost = cost;
		}
	}

	final protected String actiondescription;

	int level;
	int damagethresold;

	public ArenaBuilding(String name, String avatar, String description) {
		super(Javelin.getmonster("Building"), false);
		this.actiondescription = description;
		source.customName = name;
		source.passive = true;
		source.avatarfile = avatar;
		source.immunitytocritical = true;
		source.immunitytomind = true;
		source.immunitytoparalysis = true;
		source.immunitytopoison = true;
		setlevel(ArenaBuilding.LEVELS[0]);
	}

	void setlevel(ArenaBuilding.BuildingLevel level) {
		this.level = level.level;
		maxhp = level.hp;
		hp = maxhp;
		damagethresold = level.damagethresold;
		source.dr = level.hardness;
		source.challengerating = (level.level + 1) * 5f;
	}

	void upgrade() {
		setlevel(ArenaBuilding.LEVELS[level + 1]);
	}

	/** TODO use */
	Integer getupgradecost() {
		int next = level + 1;
		return next < ArenaBuilding.LEVELS.length
				? ArenaBuilding.LEVELS[next].cost : null;
	}

	@Override
	public void act(BattleState s) {
		s.clone(this).ap += 1;
		if (hp == maxhp) {
			source.fasthealing = 0;
		}
	}

	public boolean isrepairing() {
		return source.fasthealing > 0;
	}

	@Override
	public BattleMouseAction getmouseaction() {
		return new BattleMouseAction() {

			@Override
			public void onenter(Combatant current, Combatant target, Tile t,
					BattleState s) {
				Game.message(getactiondescription(current), Delay.NONE);
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
						} else if (click(current)) {
							s.clone(current).ap += 1;
						}
					}
				});
			}
		};
	}

	abstract protected boolean click(Combatant current);

	public String getactiondescription(Combatant current) {
		return actiondescription;
	}
}