package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

public class ArenaLair extends ArenaBuilding {
	public static final int OPTIONS = 9;

	class HireOption extends Option {
		ArrayList<Combatant> group;

		public HireOption(ArrayList<Combatant> group) {
			super(Combatant.group(group),
					calculateprice(group));
			this.group = group;
		}
	}

	class ArenaLairScreen extends PurchaseScreen {
		ArrayList<Combatant> hired = null;

		public ArenaLairScreen() {
			super("Which group of allies do you wish to hire?", null);
			stayopen = false;
		}

		@Override
		protected int getgold() {
			return ArenaFight.get().gold;
		}

		@Override
		protected void spend(Option o) {
			ArenaFight.get().gold -= o.price;
			if (o instanceof BuildingUpgradeOption) {
				((BuildingUpgradeOption) o).upgrade();
				return;
			}
			hired = ((HireOption) o).group;
			for (Combatant c : hired) {
				c.setmercenary(true);
			}
			hires.remove(hired);
			stock();
		}

		@Override
		public void onexit() {
			super.onexit();
			if (hired != null) {
				Javelin.app.switchScreen(BattleScreen.active);
				ArenaFight.get().enter(hired, Fight.state.blueTeam);
			}
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>(hires.size());
			for (ArrayList<Combatant> group : hires) {
				options.add(new HireOption(group));
			}
			return options;
		}

		@Override
		protected void sort(List<Option> options) {
			super.sort(options);
			if (getupgradecost() != null) {
				options.add(new BuildingUpgradeOption());
			}
		}
	}

	ArrayList<ArrayList<Combatant>> hires = new ArrayList<ArrayList<Combatant>>(
			OPTIONS);

	public ArenaLair() {
		super("Lair", "locationmercenariesguild",
				"Click this lair to recruit allies into the arena!");
		stock();
	}

	void stock() {
		int levelmin = level * 5 + 1;
		int levelmax = levelmin + 4;
		levelmin = CrCalculator.leveltoel(levelmin);
		levelmax = CrCalculator.leveltoel(levelmax);
		while (hires.size() < OPTIONS) {
			try {
				hires.add(EncounterGenerator.generate(RPG.r(levelmin, levelmax),
						Arrays.asList(Terrain.ALL)));
			} catch (GaveUpException e) {
				continue;
			}
		}
	}

	@Override
	protected boolean click(Combatant current) {
		new ArenaLairScreen().show();
		return true;
	}

	static double calculateprice(ArrayList<Combatant> group) {
		int fee = 0;
		for (Combatant c : group) {
			fee += MercenariesGuild.getfee(c);
		}
		return fee * 10;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current) + getgoldinfo();
	}

	public static String getgoldinfo() {
		return "\n\nYour gladiators currently have $"
				+ ShoppingScreen.formatcost(ArenaFight.get().gold) + ".";
	}

	@Override
	protected void upgradebuilding() {
		hires.clear();
		stock();
	}
}
