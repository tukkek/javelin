package javelin.model.world.location.town.labor.criminal;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * Monsters will inhabit the sewers periodically, can be upgraded for tougher
 * fights.
 *
 * @author alex
 */
public class Sewers extends Fortification {
	static final String SEWERS = "Sewers";

	public static class BuildSewers extends Build {
		public BuildSewers() {
			super("Build sewers", 5, null, Rank.HAMLET);
		}

		@Override
		public Location getgoal() {
			return new Sewers();
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && d.getlocation(Sewers.class) == null;
		}

		@Override
		protected void done(Location goal) {
			super.done(goal);
			Sewers s = (Sewers) goal;
			s.generategarrison();
		}
	}

	public class UpgradeSewers extends BuildingUpgrade {
		Sewers s;

		public UpgradeSewers(Sewers s) {
			super("", 5, 5, s, Rank.RANKS[s.level + 1]);
			this.s = s;
			name = "Upgrade sewers";
		}

		@Override
		public void done() {
			super.done();
			s.level += 1;
			s.generategarrison();
		}

		@Override
		public Location getgoal() {
			return s;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && !s.ishostile();
		}
	}

	int level = 0;

	public Sewers() {
		super(SEWERS, SEWERS, 1, 5);
		terrain = Terrain.UNDERGROUND;
		sacrificeable = false;
	}

	void generategarrison() {
		int level = 1 + this.level * 5;
		generategarrison(level, level + 4);
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		if (garrison.isEmpty() && RPG.chancein(30)) {
			generategarrison();
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (level < 3) {
			upgrades.add(new UpgradeSewers(this));
		}
		return upgrades;
	}

	@Override
	public boolean interact() {
		try {
			if (!super.interact()) {
				return false;
			}
		} catch (StartBattle e) {
			e.fight.map = RPG.pick(Terrain.UNDERGROUND.getmaps());
			throw e;
		}
		Javelin.message("The sewers are empty and safe right now...", false);
		return true;
	}
}
