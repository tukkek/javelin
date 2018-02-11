package javelin.controller.fight.minigame.arena;

import javelin.Javelin;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;

/**
 * TODO on upgrade start fast healing
 * 
 * @author alex
 */
public class ArenaBuilding extends Combatant {
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

	int level;
	int damagethresold;

	public ArenaBuilding(String name, String avatar) {
		super(Javelin.getmonster("Building"), false);
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
}