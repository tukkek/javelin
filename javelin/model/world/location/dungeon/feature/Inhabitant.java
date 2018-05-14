package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

public class Inhabitant extends Feature {
	class InhabitantFight extends Fight {
		List<Combatant> enemy;

		public InhabitantFight(List<Combatant> enemy) {
			this.enemy = enemy;
			rewardgold = false;
			bribe = false;
			hide = false;
		}

		@Override
		public ArrayList<Combatant> getmonsters(Integer teamel) {
			return new ArrayList<Combatant>(enemy);
		}

		@Override
		public String reward() {
			String rewards = super.reward();
			Squad.active.gold += gold;
			rewards += " Party receives $" + SelectScreen.formatcost(gold)
					+ "!\n";
			return rewards;
		}

		@Override
		public boolean onend() {
			boolean end = super.onend();
			for (Key k : keys) {
				k.grab();
			}
			return end;
		}
	}

	class SellKey extends Option {
		Key item;

		SellKey(Key k) {
			super("Buy " + k.name.toLowerCase(), k.price);
			item = k;
		}
	}

	class InhabitantScreen extends SelectScreen {
		List<Combatant> enemy = Arrays.asList(new Combatant[] { inhabitant });
		Option extractinformation = new Option("Extract information", 0);
		Option sellinformation = new Option("Buy information",
				hints * RewardCalculator.getgold(Dungeon.active.level) / 10);
		Option attack = new Option(
				"Attack (" + Difficulty.describe(enemy) + ")", 0);
		Option hire = new Option("Hire", MercenariesGuild.getfee(inhabitant));

		public InhabitantScreen() {
			super("You encounter a friendly " + inhabitant + "!", null);
		}

		@Override
		public String getCurrency() {
			return "$";
		}

		@Override
		public String printpriceinfo(Option o) {
			return o.price == 0 ? "" : super.printpriceinfo(o);
		}

		@Override
		public String printinfo() {
			return "You have $" + SelectScreen.formatcost(Squad.active.gold)
					+ ".";
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>();
			int diplomacy = Squad.active.getbest(Skill.DIPLOMACY)
					.taketen(Skill.DIPLOMACY);
			if (diplomacy >= basedc + 5) {
				options.add(hire);
			}
			if (diplomacy >= basedc) {
				for (Key k : keys) {
					options.add(new SellKey(k));
				}
			}
			if (hints > 0) {
				if (diplomacy >= basedc) {
					options.add(extractinformation);
				} else if (diplomacy >= basedc - 5) {
					options.add(sellinformation);
				}
			}
			options.add(attack);
			while (options.contains(null)) {
				options.remove(null);
			}
			return options;
		}

		@Override
		public boolean select(Option o) {
			if (o.price > Squad.active.gold) {
				print(text + "\nNot enough gold...");
				return false;
			}
			Squad.active.gold -= o.price;
			gold += o.price;
			if (o instanceof SellKey) {
				return sellkey(((SellKey) o).item);
			}
			if (o == extractinformation || o == sellinformation) {
				return getinformation();
			}
			if (o == hire) {
				return hire();
			}
			if (o == attack) {
				return attack();
			}
			throw new RuntimeException("#unknowninhabitantoption");
		}

		boolean attack() {
			Dungeon.active.features.remove(Inhabitant.this);
			throw new StartBattle(new InhabitantFight(enemy));
		}

		boolean hire() {
			inhabitant.setmercenary(true);
			Squad.active.members.add(inhabitant);
			Dungeon.active.features.remove(Inhabitant.this);
			hire = null;
			attack = null;
			return true;
		}

		boolean getinformation() {
			for (int i = 0; i < hints; i++) {
				Feature f = Spirit.findtarget();
				if (f != null) {
					Spirit.discover(f);
				}
			}
			hints = 0;
			return true;
		}

		boolean sellkey(Key k) {
			k.grab();
			keys.remove(k);
			return true;
		}
	}

	ArrayList<Key> keys = new ArrayList<Key>();
	int basedc = 10 + Dungeon.active.level;
	int hints = RPG.r(1, 10);
	Combatant inhabitant;
	int gold;

	public Inhabitant(int xp, int yp) {
		super(xp, yp, null);
		remove = false;
		initinhabitant();
		initkeys(RPG.r(1, 4) - 1);
	}

	void initkeys(int nkeys) {
		for (int i = 0; i < nkeys; i++) {
			keys.add(Key.generate());
		}
	}

	void initinhabitant() {
		int crmin = Dungeon.active.level + Difficulty.DIFFICULT;
		int crmax = Dungeon.active.level + Difficulty.DEADLY;
		Monster m = getmonster(crmin, crmax);
		inhabitant = new Combatant(m, true);
		Monster source = inhabitant.source;
		avatarfile = source.avatarfile;
		gold = RewardCalculator.getgold(source.cr) * RPG.r(0, 100) / 100;
	}

	Monster getmonster(int crmin, int crmax) {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for (Float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (crmin <= cr && cr <= crmax) {
				candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		}
		Collections.shuffle(candidates);
		for (Monster m : candidates) {
			if (m.think(+1)) {
				return m;
			}
		}
		return getmonster(crmin - 1, crmax + 1);
	}

	@Override
	public boolean activate() {
		new InhabitantScreen().show();
		return false;
	}
}
