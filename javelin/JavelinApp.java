package javelin;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Wand;
import javelin.model.spell.Summon;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;
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
	static String[] ERRORQUOTES =
			new String[] { "A wild error appears!", "You were eaten by a grue.",
					"So again it has come to pass...", "Mamma mia!", };

	/** View for the {@link World}. */
	public static BattleMap overviewmap;
	/** Controller. */
	static public WorldScreen context;
	static ArrayList<Monster> lastenemies = new ArrayList<Monster>();

	/**
	 * Keeps track of monster status before combat so we can restore any
	 * temporary effects.
	 */
	public ArrayList<Combatant> originalteam;
	/**
	 * TODO see {@link BattleScreen#originalredteam} }
	 */
	public ArrayList<Combatant> originalfoes;
	/**
	 * Active battle map;
	 */
	public BattleMap battlemap;
	/**
	 * Controller for active battle. Should be <code>null</code> at any point a
	 * battle is not occurring.
	 */
	public Fight fight;
	public JFrame frame;

	@Override
	public void run() {
		javelin.controller.db.Preferences.init();// pre
		initialize();
		if (!StateManager.load()) {
			if (StateManager.nofile) {
				disclaimer();
			}
			startcampaign();
		}
		javelin.controller.db.Preferences.init();// post
		placesquads();
		preparedebug();
		if (Dungeon.active != null) {
			Dungeon.active.activate(true);
		}
		StateManager.save(true, StateManager.SAVEFILE);
		if (Javelin.DEBUG) {
			while (true) {
				loop();
			}
		} else {
			while (true) {
				try {
					loop();
				} catch (RuntimeException e) {
					handlefatalexception(e);
				}
			}
		}
	}

	void loop() {
		try {
			if (Dungeon.active == null) {
				JavelinApp.context = new WorldScreen(JavelinApp.overviewmap);
			}
			while (true) {
				switchScreen(JavelinApp.context);
				JavelinApp.context.step();
			}
		} catch (final StartBattle e) {
			fight = e.fight;
			if (Squad.active.strategic && fight instanceof RandomEncounter) {
				/*
				 * TODO support all types of strategic battles (Lair, Incursion,
				 * Siege...)
				 */
				quickbattle();
			} else {
				e.battle();
			}
		}
	}

	private void quickbattle() {
		int teamel =
				ChallengeRatingCalculator.calculateel(Squad.active.members);
		List<Combatant> opponents = fight.getmonsters(teamel);
		int el = opponents != null
				? ChallengeRatingCalculator.calculateel(opponents)
				: fight.getel(teamel);
		if (opponents == null) {
			opponents = fight.generate(teamel, Terrain.current());
		}
		if (fight.avoid(opponents)) {
			return;
		}
		float resourcesused =
				ChallengeRatingCalculator.useresources(teamel, el);
		ArrayList<Combatant> original =
				new ArrayList<Combatant>(Squad.active.members);
		for (Combatant c : original) {
			strategicdamage(c, resourcesused);
		}
		if (Squad.active.members.isEmpty()) {
			Javelin.message("Battle report: Squad lost in combat!", false);
			Squad.active.disband();
			return;
		}
		BattleMap.victory = true;
		preparebattle(opponents);
		switchScreen(WorldScreen.current);
		EndBattle.showcombatresult(WorldScreen.active, original,
				"Battle report: ");
	}

	void strategicdamage(Combatant c, float resourcesused) {
		c.hp -= c.maxhp * resourcesused;
		if (c.hp <= Combatant.DEADATHP || //
				(c.hp <= 0 && RPG.random() < Math
						.abs(c.hp / new Float(Combatant.DEADATHP)))) {
			Squad.active.members.remove(c);
			return;
		}
		if (c.hp <= 0) {
			c.hp = 1;
		}
		for (Spell s : c.spells) {
			for (int i = s.used; i < s.perday; i++) {
				if (RPG.random() < resourcesused) {
					s.used += 1;
				}
			}
		}
		ArrayList<Item> bag = Squad.active.equipment.get(c.id);
		for (Item i : new ArrayList<Item>(bag)) {
			if (i.usedinbattle) {
				if (i instanceof Wand) {
					Wand w = (Wand) i;
					w.charges -= w.charges * resourcesused;
					if (w.charges <= 0) {
						bag.remove(w);
					}
				} else if (RPG.random() < resourcesused) {
					bag.remove(i);
				}
			}
		}
	}

	/**
	 * @param e
	 *            Show this error to the user and log it.
	 */
	static public void handlefatalexception(RuntimeException e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(Javelin.app,
				RPG.pick(ERRORQUOTES) + "\n\nUnfortunately an error ocurred.\n"
						+ "Please send a screenshot of the next message or the log file (error.txt)\n"
						+ "to one of the channels below, so we can get this fixed on future releases.\n\n"
						+ "If for any reason your current game fails to load when you restart Javelin\n"
						+ "you can find backups on the \"backup\" folder. Just move one of them to the\n"
						+ "main folder and rename it to \"javelin.save\" to restore your progress.\n\n"
						+ "Post to our reddit forum -- http://www.reddit.com/r/javelinrl\n"
						+ "Leave a comment on our website -- http://javelinrl.wordpress.com\n"
						+ "Or write us an e-mail -- javelinrl@gmail.com\n");
		String error = printstack(e);
		Throwable t = e;
		HashSet<Throwable> errors = new HashSet<Throwable>(2);
		while (t.getCause() != null && errors.add(t)) {
			t = t.getCause();
			error += System.lineSeparator() + printstack(t);
		}
		try {
			Preferences.write(error, "error.txt");
		} catch (IOException e1) {
			// ignore
		}
		JOptionPane.showMessageDialog(Javelin.app, error);
		System.exit(1);
	}

	static String printstack(Throwable e) {
		String error = e.getMessage() + " (" + e.getClass() + ")";
		error += "\n";
		for (StackTraceElement stack : e.getStackTrace()) {
			error += stack.toString() + "\n";
		}
		return error;
	}

	void disclaimer() {
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
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ThreadManager.determineprocessors();
	}

	void placesquads() {
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

	void startcampaign() {
		SquadScreen.open();
		World.makemap();
		UpgradeHandler.singleton.distribute();
		Item.distribute();
		if (Javelin.DEBUG) {
			JavelinApp.printstatistics();
		}
	}

	static void printstatistics() {
		System.out.println();
		HashMap<String, HashSet<Upgrade>> allupgrades =
				UpgradeHandler.singleton.getall();
		HashMap<String, ItemSelection> allitems = Item.getall();
		gatherstatistics(allupgrades, allitems);
		System.out.println(Javelin.ALLMONSTERS.size() + " monsters");
		System.out.println((Item.ALL.size() - Item.ARTIFACT.size()) + " items, "
				+ Item.ARTIFACT.size() + " artifacts, 7 relics");
		Collection<Spell> spells = Spell.SPELLS.values();
		int summon = countsummon(spells);
		System.out.println((UpgradeHandler.singleton.count() - spells.size())
				+ " upgrades, " + (spells.size() - summon + 1) + " spells, "
				+ UpgradeHandler.singleton.countskills() + " skills");
		int maps = Dungeon.getmaps().size();
		for (Terrain t : Terrain.ALL) {
			maps += t.getmaps().size();
		}
		System.out.println(maps + " battle maps");
	}

	static void gatherstatistics(HashMap<String, HashSet<Upgrade>> allupgrades,
			HashMap<String, ItemSelection> allitems) {
		for (String realm : allupgrades.keySet()) {
			if (realm == "expertise" || realm == "shots" || realm == "power"
					|| realm.startsWith("school")) {
				continue;
			}
			System.out.println(realm);
			int count = 1;
			HashSet<Upgrade> upgrades = allupgrades.get(realm);
			for (Upgrade u : upgrades) {
				System.out.println("\t" + count + " - " + u);
				count += 1;
			}
			System.out.println();
			ItemSelection inventory = allitems.get(realm);
			for (int i = 0; i < inventory.size(); i++) {
				Item item = inventory.get(i).clone();
				item.shop();
				System.out.println(
						"\t" + count + " - " + item + " ($" + item.price + ")");
				count += 1;
			}
			System.out.println();
		}
	}

	static int countsummon(Collection<Spell> spells) {
		int summon = 0;
		for (Spell s : spells) {
			if (s instanceof Summon) {
				summon += 1;
			}
		}
		return summon;
	}

	void preparedebug() {
		if (Preferences.DEBUGSGOLD != null) {
			Squad.active.gold = Preferences.DEBUGSGOLD;
		}
		if (Preferences.DEBUGSXP != null) {
			for (final Combatant m : Squad.active.members) {
				m.xp = new BigDecimal(Preferences.DEBUGSXP / 100f);
			}
		}
		if (Preferences.DEBUGRUBIES != null) {
			Haxor.singleton.rubies = Preferences.DEBUGRUBIES;
		}
		if (Preferences.DEBUGSTARTINGITEM != null) {
			Squad.active.receiveitem(Preferences.DEBUGSTARTINGITEM);
		}
		if (Preferences.DEBUGLABOR != null) {
			for (WorldActor p : Town.getall(Town.class)) {
				Town t = (Town) p;
				if (t.garrison.isEmpty()) {
					t.labor = Preferences.DEBUGLABOR;
				}
			}
		}
	}

	/** TODO deduplicate originals */
	public void preparebattle(Collection<Combatant> opponents) {
		BattleMap.blueTeam = new ArrayList<Combatant>(Squad.active.members);
		BattleMap.redTeam = new ArrayList<Combatant>(opponents);
		JavelinApp.lastenemies.clear();
		for (final Combatant m : BattleMap.redTeam) {
			JavelinApp.lastenemies.add(m.source.clone());
		}
		originalteam = JavelinApp.cloneteam(BattleMap.blueTeam);
		originalfoes = JavelinApp.cloneteam(BattleMap.redTeam);
		BattleScreen.originalblueteam =
				new ArrayList<Combatant>(BattleMap.blueTeam);
		BattleScreen.originalredteam =
				new ArrayList<Combatant>(BattleMap.redTeam);
	}

	private static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team) {
		ArrayList<Combatant> clone = new ArrayList<Combatant>(team.size());
		for (Combatant c : team) {
			clone.add(c.clone());
		}
		return clone;
	}

	@Override
	public void preparebattlemap() {

	}

	@Override
	public void setupScreen() {
	}
}