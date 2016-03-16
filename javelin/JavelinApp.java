package javelin;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.BattleSetup;
import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.db.StateManager;
import javelin.controller.encounter.EncounterGenerator;
import javelin.controller.encounter.GeneratedFight;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.LairFight;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Dungeon;
import javelin.model.world.Haxor;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.town.Town;
import javelin.view.SquadScreen;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Application and game life-cycle.
 * 
 * @author alex
 */
public class JavelinApp extends QuestApp {
	private static final long serialVersionUID = 1L;

	/**
	 * Keeps track of monster status before combat so we can restore any
	 * temporary effects.
	 */
	ArrayList<Combatant> originalteam;
	public ArrayList<Combatant> originalfoes;

	private BattleMap battlemap;

	public Fight fight;

	public static BattleMap overviewmap;

	static public WorldScreen context;

	@Override
	public void run() {
		initialize();
		if (!StateManager.load()) {
			if (StateManager.nofile) {
				disclaimer();
			}
			startcampaign();
		}
		placesquads();
		preparedebug();
		if (Dungeon.active != null) {
			Dungeon.active.activate();
		}
		while (true) {
			try {
				if (Dungeon.active == null) {
					JavelinApp.context =
							new WorldScreen(JavelinApp.overviewmap);
				}
				while (true) {
					switchScreen(JavelinApp.context);
					StateManager.save();
					JavelinApp.context.step();
				}
			} catch (final StartBattle e) {
				battle(e);
			}
		}
	}

	public void disclaimer() {
		while (TextReader
				.show(new File("README.txt"),
						"This message will only be shown once, press ENTER to continue.")
				.getKeyCode() != KeyEvent.VK_ENTER) {
			// wait for enter
		}
	}

	private void initialize() {
		try {
			BufferedReader reader =
					new BufferedReader(new FileReader(new File("README.txt")));
			String line = reader.readLine();
			while (line != null) {
				if (!line.trim().isEmpty()) {
					System.out.println(line);
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ThreadManager.determineprocessors();
	}

	public void battle(final StartBattle start) {
		fight = start.fight;
		BattleMap.blueTeam = Squad.active.members;
		int teamel =
				ChallengeRatingCalculator.calculateElSafe(BattleMap.blueTeam);
		List<Combatant> foes = start.fight.getmonsters(teamel);
		if (foes == null) {
			foes = JavelinApp
					.generatefight(start.fight.getel(this, teamel)).opponents;
			/* TODO enhance Fight hierarchy with these: */
			while (foes.size() == 1 && start.fight instanceof LairFight) {
				foes = JavelinApp.generatefight(
						start.fight.getel(this, teamel)).opponents;
			}
			if (fight instanceof IncursionFight) {
				((IncursionFight) fight).incursion.squad =
						Incursion.getsafeincursion(foes);
			}
		}
		JavelinApp.preparebattle(new GeneratedFight(foes));
		final BattleScreen battleScreen =
				start.fight.getscreen(this, battlemap);
		try {
			battleScreen.mainLoop();
		} catch (final EndBattle end) {
			int nsquads = Squad.squads.size();
			battleScreen.onEnd();
			BattleMap.combatants.clear();
			AiCache.reset();
			/* TODO probably size comparison is enough */
			if (Squad.active != null && nsquads == Squad.squads.size()) {
				while (WorldScreen.getactor(Squad.active.x, Squad.active.y,
						Incursion.squads) != null) {
					Squad.active.visual.remove();
					Squad.active.displace();
					Squad.active.visual.remove();
					Squad.active.place();
				}
				endbattle();
				if (Dungeon.active != null) {
					Dungeon.active.activate();
				}
			}
		}
	}

	public void placesquads() {
		for (final Squad s : Squad.squads) {
			final Thing hero = s.visual;
			hero.x = s.x;
			hero.y = s.y;
			if (hero.place == null) {
				JavelinApp.overviewmap.addThing(hero, hero.x, hero.y);
			}
		}
	}

	public void startcampaign() {
		SquadScreen.open();
		WorldScreen.makemap();
		new UpgradeHandler().distribute();
		Item.distribute();
		if (Javelin.DEBUG) {
			JavelinApp.debugoptions();
		}
	}

	static void debugoptions() {
		int totalupgrades = 0;
		for (Town t : Town.towns) {
			System.out.println();
			System.out.println(t);
			int noptions = 1;
			for (int i = 0; i < t.items.size(); i++, noptions++) {
				Item it = t.items.get(i);
				System.out.println(
						"    " + noptions + " - " + it + " $" + it.price);
			}
			System.out.println();
			for (int i = 0; i < t.upgrades.size(); i++, noptions++) {
				System.out
						.println("    " + noptions + " - " + t.upgrades.get(i));
			}
			totalupgrades += t.upgrades.size();
		}
		System.out.println();
		System.out.println(Item.ALL.size() + " items");
		System.out.println(totalupgrades + " upgrades (" + SpellsFactor.spells
				+ " spells)");
	}

	public void preparedebug() {
		if (Javelin.DEBUGSTARTINGGOLD != null) {
			Squad.active.gold = Javelin.DEBUGSTARTINGGOLD;
		}
		if (Javelin.DEBUGSTARTINGXP != null) {
			for (final Combatant m : Squad.active.members) {
				m.xp = new BigDecimal(Javelin.DEBUGSTARTINGXP / 100f);
			}
		}
		if (Javelin.DEBUGSTARTINGHAX != null) {
			Haxor.singleton.tickets = Javelin.DEBUGSTARTINGHAX;
		}
		// TODO used for debug
		if (Javelin.DEBUGSTARTINGKEY != null) {
			Squad.active.receiveitem(Key.generate(Javelin.DEBUGSTARTINGKEY));
		}
	}

	private void endbattle() {
		for (Combatant c : new ArrayList<Combatant>(BattleMap.combatants)) {
			if (c.summoned) {
				BattleMap.combatants.remove(c);
				BattleMap.blueTeam.remove(c);
				BattleMap.redTeam.remove(c);
			}
		}
		EndBattle.updateoriginal(originalteam);
		EndBattle.bury(originalteam);
		if (Javelin.captured != null) {
			originalteam.add(Javelin.captured);
			Javelin.captured = null;
		}
		Squad.active.members = originalteam;
		for (Combatant member : Squad.active.members) {
			member.currentmelee.sequenceindex = -1;
			member.currentranged.sequenceindex = -1;
		}
		ThreadManager.printbattlerecord();
	}

	static ArrayList<Monster> lastenemies = new ArrayList<Monster>();

	public static void preparebattle(GeneratedFight generated) {
		BattleMap.redTeam = new ArrayList<Combatant>();
		BattleMap.redTeam.addAll(generated.opponents);
		JavelinApp.lastenemies.clear();
		for (final Combatant m : BattleMap.redTeam) {
			JavelinApp.lastenemies.add(m.source.clone());
		}
		Javelin.app.originalteam = JavelinApp.cloneteam(BattleMap.blueTeam);
		Javelin.app.originalfoes = JavelinApp.cloneteam(BattleMap.redTeam);
		Javelin.app.battlemap = BattleSetup.place();
	}

	private static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team) {
		ArrayList<Combatant> clone = new ArrayList<Combatant>(team.size());
		for (Combatant c : team) {
			clone.add(c.clone());
		}
		return clone;
	}

	public static GeneratedFight generatefight(final int el) {
		int delta = 0;
		GeneratedFight generated = null;
		while (generated == null) {
			generated = JavelinApp.chooseopponents(el - delta);
			if (generated != null) {
				break;
			}
			if (delta != 0) {
				generated = JavelinApp.chooseopponents(el + delta);
			}
			delta += 1;
		}
		return generated;
	}

	static public GeneratedFight chooseopponents(final int el) {
		try {
			return new GeneratedFight(EncounterGenerator.generate(el));
		} catch (final GaveUpException e) {
			return null;
		}
	}

	/**
	 * 2 chances of an easy encounter, 10 chances of a moderate encounter, 4
	 * chances of a difficult encounter and 1 chance of an overwhelming
	 * encounter
	 */
	public static Integer randomdifficulty() {
		final LinkedList<Integer> elchoices = new LinkedList<Integer>();
		for (int j = 0; j < 2; j++) {
			elchoices.add(RPG.r(-5, -8));
		}
		for (int j = 0; j < 10; j++) {
			elchoices.add(-4);
		}
		for (int j = -3; j <= 0; j++) {
			elchoices.add(j);
		}
		elchoices.add(1);
		return RPG.pick(elchoices);
	}

	@Override
	public void preparebattlemap() {

	}

	@Override
	public void setupScreen() {
	}
}