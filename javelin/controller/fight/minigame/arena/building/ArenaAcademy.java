package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.view.screen.upgrading.UpgradingScreen;
import tyrant.mikera.engine.RPG;

public class ArenaAcademy extends ArenaBuilding {
	static final int NOPTIONS = 9;

	class ArenaAcademyScreen extends UpgradingScreen {
		Combatant trainee;

		public ArenaAcademyScreen(Combatant c) {
			super("What will you learn, " + c + "?", null);
			this.trainee = c;
		}

		@Override
		protected void registertrainee(Order trainee) {
			ArrayList<Combatant> team = Fight.state.blueTeam;
			Combatant c = ((TrainingOrder) trainee).trained;
			team.set(team.indexOf(c), c);
		}

		@Override
		protected Collection<Upgrade> getupgrades() {
			restock(trainee);
			return upgrades;
		}

		@Override
		public TrainingOrder createorder(Combatant c, Combatant original,
				float xpcost) {
			return new TrainingOrder(c, null, c.toString(), xpcost, original);
		}

		@Override
		public ArrayList<Combatant> gettrainees() {
			ArrayList<Combatant> trainee = new ArrayList<Combatant>(1);
			trainee.add(this.trainee);
			return trainee;
		}

		@Override
		public int getgold() {
			return ArenaFight.get().gold;
		}

		@Override
		public void pay(int cost) {
			ArenaFight.get().gold -= cost;
		}

		@Override
		protected Integer getperiod(float cost) {
			return null;
		}
	}

	HashSet<Upgrade> upgrades = new HashSet<Upgrade>(NOPTIONS);

	public ArenaAcademy() {
		super("Academy", "locationrealmacademy",
				"Click this academy to upgrade the active unit!");
	}

	public void restock(Combatant trainee) {
		if (upgrades.size() == NOPTIONS) {
			return;
		}
		if (upgrades.isEmpty()) {
			upgrades.add(ClassLevelUpgrade.classes[RPG.r(0,
					ClassLevelUpgrade.classes.length - 1)]);
		}
		LinkedList<Upgrade> allupgrades = new LinkedList<Upgrade>(
				UpgradeHandler.singleton.getalluncategorized());
		while (upgrades.size() < NOPTIONS) {
			Combatant clone = trainee.clone().clonesource();
			Upgrade u = allupgrades.pop();
			if (u.upgrade(clone)) {
				upgrades.add(u);
			}
		}
	}

	@Override
	protected boolean click(Combatant current) {
		new ArenaAcademyScreen(current).show();
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current) + "\n\n" + current
				+ " currently has " + current.gethumanxp() + ".";
	}
}
