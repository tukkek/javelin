package javelin;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.BattleSetup;
import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
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
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Application and game life-cycle.
 * 
 * TODO refactor a time manager out of this which works with
 * TimeManager#tick(int time,WorldState world)?
 * 
 * @author alex
 */
public class JavelinApp extends QuestApp {
	private static final long serialVersionUID = 1L;

	public static BattleMap overviewmap;
	static public WorldScreen context;
	static ArrayList<Monster> lastenemies = new ArrayList<Monster>();

	/**
	 * Keeps track of monster status before combat so we can restore any
	 * temporary effects.
	 */
	ArrayList<Combatant> originalteam;
	public ArrayList<Combatant> originalfoes;
	private BattleMap battlemap;
	public Fight fight;

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
			int currentterrain = Javelin.terrain();
			foes = JavelinApp.generatefight(start.fight.getel(this, teamel),
					currentterrain).opponents;
			/* TODO enhance Fight hierarchy with these: */
			while (foes.size() == 1 && start.fight instanceof LairFight) {
				foes = JavelinApp.generatefight(start.fight.getel(this, teamel),
						currentterrain).opponents;
			}
			if (fight instanceof IncursionFight) {
				((IncursionFight) fight).incursion.squad =
						Incursion.getsafeincursion(foes);
			}
		}
		/* TODO enable in Dungeon as well, debug */
		if (Dungeon.active == null) {
			if (fight.hide() && Squad.active.hide(foes)) {
				return;
			}
			if (fight.canbribe() && Squad.active.bribe(foes)) {
				fight.bribe();
				return;
			}
		}
		JavelinApp.preparebattle(new GeneratedFight(foes));
		final BattleScreen battleScreen =
				start.fight.getscreen(this, battlemap);
		try {
			battleScreen.mainLoop();
		} catch (final EndBattle end) {
			EndBattle.end(battleScreen, originalteam);
		}
	}

	public void placesquads() {
		for (final WorldActor a : Squad.getall(Squad.class)) {
			Squad s = (Squad) a;
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
		World.makemap();
		UpgradeHandler.singleton.distribute();
		Item.distribute();
		if (Javelin.DEBUG) {
			JavelinApp.debugoptions();
		}
	}

	static void debugoptions() {
		HashMap<String, List<Upgrade>> allupgrades =
				UpgradeHandler.singleton.getall();
		HashMap<String, ItemSelection> allitems = Item.getall();
		for (String realm : allupgrades.keySet()) {
			if (realm == "expertise" || realm == "shots" || realm == "power") {
				continue;
			}
			System.out.println(realm);
			int count = 1;
			List<Upgrade> upgrades = allupgrades.get(realm);
			for (int i = 0; i < upgrades.size(); i++) {
				System.out.println("\t" + count + " - " + upgrades.get(i));
				count += 1;
			}
			System.out.println();
			ItemSelection inventory = allitems.get(realm);
			for (int i = 0; i < inventory.size(); i++) {
				System.out.println("\t" + count + " - " + inventory.get(i));
				count += 1;
			}
			System.out.println();
		}
		System.out.println();
		System.out.println(Javelin.ALLMONSTERS.size() + " monsters");
		System.out.println((Item.ALL.size() - Item.ARTIFACT.size()) + " items, "
				+ Item.ARTIFACT.size() + " artifacts");
		System.out.println(
				(UpgradeHandler.singleton.count() - SpellsFactor.spells)
						+ " upgrades, " + SpellsFactor.spells + " spells, "
						+ UpgradeHandler.singleton.countskills() + " skills");
	}

	void preparedebug() {
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
		if (Javelin.DEBUGSTARTINGITEM != null) {
			Squad.active.receiveitem(Javelin.DEBUGSTARTINGITEM);
		}
		if (Javelin.DEBUGSTARTINGLABOR != null) {
			for (WorldActor p : Town.getall(Town.class)) {
				Town t = (Town) p;
				if (t.garrison.isEmpty()) {
					t.labor = Javelin.DEBUGSTARTINGLABOR;
				}
			}
		}
	}

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

	public static GeneratedFight generatefight(final int el, int terrainp) {
		int delta = 0;
		GeneratedFight generated = null;
		while (generated == null) {
			generated = JavelinApp.chooseopponents(el - delta, terrainp);
			if (generated != null) {
				break;
			}
			if (delta != 0) {
				generated = JavelinApp.chooseopponents(el + delta, terrainp);
			}
			delta += 1;
		}
		return generated;
	}

	static public GeneratedFight chooseopponents(final int el, int terrainp) {
		try {
			return new GeneratedFight(
					EncounterGenerator.generate(el, terrainp));
		} catch (final GaveUpException e) {
			return null;
		}
	}

	@Override
	public void preparebattlemap() {

	}

	@Override
	public void setupScreen() {
	}
}