package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.upgrading.UpgradingScreen;

public class ArenaAcademy extends ArenaBuilding {
	static final int NOPTIONS = 9;

	class ArenaAcademyScreen extends UpgradingScreen {
		Combatant trainee;

		public ArenaAcademyScreen(Combatant c) {
			super("What will you learn, " + c + "?", null);
			trainee = c;
			restock(trainee);
		}

		@Override
		protected void registertrainee(Order trainee) {
			ArrayList<Combatant> team = Fight.state.blueTeam;
			Combatant c = ((TrainingOrder) trainee).trained;
			team.set(team.indexOf(c), c);
		}

		@Override
		protected Collection<Upgrade> getupgrades() {
			return upgrades;
		}

		@Override
		public List<Option> getoptions() {
			List<Option> options = super.getoptions();
			if (getupgradecost() != null) {
				options.add(new BuildingUpgradeOption());
			}
			return options;
		}

		@Override
		public boolean select(Option op) {
			BuildingUpgradeOption upgrade = op instanceof BuildingUpgradeOption
					? (BuildingUpgradeOption) op : null;
			if (upgrade == null) {
				return super.select(op);
			}
			stayopen = false;
			return upgrade.buy(this);
		}

		@Override
		public String printpriceinfo(Option o) {
			BuildingUpgradeOption upgrade = o instanceof BuildingUpgradeOption
					? (BuildingUpgradeOption) o : null;
			if (upgrade == null) {
				return super.printpriceinfo(o);
			}
			return " $" + SelectScreen.formatcost(o.price);
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

		@Override
		public String printinfo() {
			return "Your gladiators have $"
					+ PurchaseScreen.formatcost(ArenaFight.get().gold);
		}
	}

	HashSet<Upgrade> upgrades = new HashSet<Upgrade>(NOPTIONS);

	public ArenaAcademy() {
		super("Academy", "locationrealmacademy",
				"Click this academy to upgrade the active unit!");
	}

	void restock(Combatant trainee) {
		upgrades.clear();
		upgrades.add(ClassLevelUpgrade.getpreferred(trainee));
		LinkedList<Upgrade> all = new LinkedList<Upgrade>(
				UpgradeHandler.singleton.getalluncategorized());
		Collections.shuffle(all);
		while (upgrades.size() < 3 + level && !all.isEmpty()) {
			Upgrade u = all.pop();
			Combatant clone = trainee.clone().clonesource();
			Spell s = u instanceof Spell ? (Spell) u : null;
			if (s != null && !s.castinbattle) {
				continue;
			}
			if (u.usedincombat && u.upgrade(clone)) {
				upgrades.add(u);
			}
		}
	}

	@Override
	protected void upgradebuilding() {
		upgrades.clear();
	}

	@Override
	protected boolean click(Combatant current) {
		new ArenaAcademyScreen(current).show();
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current) + "\n\n" + current
				+ " currently has " + current.gethumanxp() + ". Your team has $"
				+ PurchaseScreen.formatcost(ArenaFight.get().gold) + ".";
	}
}
