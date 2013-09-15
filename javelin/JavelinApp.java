package javelin;

import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.BattlePlacement;
import javelin.controller.TextReader;
import javelin.controller.Weather;
import javelin.controller.ai.ThreadManager;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.encounter.EncounterGenerator;
import javelin.controller.encounter.GeneretadFight;
import javelin.controller.exception.EndBattle;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.StartBattle;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.BattleMap;
import javelin.model.item.Item;
import javelin.model.item.RaiseScroll;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.view.SquadScreen;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.QuestApp;

public class JavelinApp extends QuestApp {
	private static final long serialVersionUID = 1L;
	public static final boolean OVERWORLD = true;

	/**
	 * Keeps track of monster status before combat so we can restore any
	 * temporary effects.
	 */
	private ArrayList<Combatant> originalteam;
	public ArrayList<Combatant> originalfoes;

	private BattleMap battlemap;

	public static BattleMap overviewmap;

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
		while (true) {
			try {
				final WorldScreen overworld = new WorldScreen(this, overviewmap);
				while (true) {
					StateManager.save();
					overworld.step();
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
		System.out
				.println("Copyright (C) 2015 Alex Henry\n\n"
						+ "This program is free software; you can redistribute it and/or modify it under\n"
						+ "the terms of the GNU General Public License version 2 as published by the Free\n"
						+ "Software Foundation.\n"
						+ "A copy of the full license text is available inside the 'doc' directory.\n"
						+ "See our website for information on how to contact the developers:\n"
						+ "http://javelinrl.wordpress.com or http://github.com/tukkek/javelin\n");
		ThreadManager.determineprocessors();
	}

	public void battle(final StartBattle e) {
		GeneretadFight f;
		int teamel = ChallengeRatingCalculator
				.calculateSafe(ChallengeRatingCalculator
						.convertlist(BattleMap.blueTeam));
		List<Monster> foes = e.fight.getmonsters(teamel);
		if (foes == null) {
			foes = generatefight(e.fight.getel(this, teamel)).opponents;
		}
		preparebattle(new GeneretadFight(foes));
		final BattleScreen battleScreen = e.fight.getscreen(this, battlemap);
		try {
			battleScreen.mainLoop();
		} catch (final EndBattle e2) {
			battleScreen.onEnd();
			BattleMap.combatants.clear();
			battleScreen.afterend();
			if (Squad.active != null) {
				endbattle();
			}
		}
	}

	public void placesquads() {
		for (final Squad s : Squad.squads) {
			final Thing hero = s.visual;
			hero.x = s.x;
			hero.y = s.y;
			if (hero.place == null) {
				overviewmap.addThing(hero, hero.x, hero.y);
			}
		}
	}

	public void startcampaign() {
		selectsquad();
		WorldScreen.makemap();
		new UpgradeHandler().distribute();
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
	}

	public void endbattle() {
		for (final Combatant inbattle : BattleMap.blueTeam) {
			for (final Combatant original : originalteam) {
				if (original.source.customName == inbattle.source.customName) {
					original.hp = inbattle.hp;
					if (original.hp > original.maxhp) {
						original.hp = original.maxhp;
					}
					original.xp = inbattle.xp;
					copyspells(inbattle, original);
					break;
				}
			}
		}
		for (final Combatant dead : BattleMap.dead) {
			for (final Combatant original : originalteam) {
				if (dead.id == original.id) {
					original.hp = dead.hp;
					break;
				}
			}
		}
		BattleMap.dead.clear();
		for (final Combatant original : new ArrayList<Combatant>(originalteam)) {
			checkfordead(original);
		}
		if (Javelin.captured != null) {
			originalteam.add(Javelin.captured);
			Javelin.captured = null;
		}
		BattleMap.blueTeam = originalteam;
		Squad.active.members = originalteam;
		for (Combatant member : Squad.active.members) {
			member.currentmelee.sequenceindex = -1;
			member.currentranged.sequenceindex = -1;
		}
	}

	public void copyspells(final Combatant from, final Combatant to) {
		for (int i = 0; i < from.spells.size(); i++) {
			to.spells.get(i).used = from.spells.get(i).used;
		}
	}

	private void checkfordead(Combatant original) {
		for (final Combatant m : BattleMap.blueTeam) {
			if (m.id == original.id) {
				return;
			}
		}
		if (Combatant.DEADATHP < original.hp && original.hp <= 0) {
			original.hp = 1;
		} else if (!revive(original)) {
			originalteam.remove(original);
			Squad.active.equipment.remove(original.toString());
		}

	}

	public boolean revive(Combatant original) {
		final List<Item> items = new ArrayList<Item>();
		for (final List<Item> i : Squad.active.equipment.values()) {
			items.addAll(i);
		}
		Item revive = null;
		for (final Item i : items) {
			if (i instanceof RaiseScroll) {
				revive = i;
				break;
			}
		}
		if (revive != null
				&& revive.use(new Combatant(null, original.source, false))) {
			for (final List<Item> i : Squad.active.equipment.values()) {
				if (i.remove(revive)) {
					return true;
				}
			}
		}
		return false;
	}

	static ArrayList<Monster> lastenemies = new ArrayList<Monster>();

	public static void preparebattle(GeneretadFight generated) {
		BattleMap.redTeam = new ArrayList<Combatant>();
		for (Monster m : generated.opponents) {
			BattleMap.redTeam.add(new Combatant(null, m, true));
		}
		Javelin.mapType = Javelin.DEBUGMAPTYPE == null ? RPG.pick(MAPTYPES)
				: Javelin.DEBUGMAPTYPE;
		lastenemies.clear();
		for (final Combatant m : BattleMap.redTeam) {
			lastenemies.add(m.source.clone());
		}
		copyoriginal();
		Javelin.app.battlemap = BattlePlacement.place();
		Weather.flood(Javelin.app.battlemap);
		for (final Combatant c : BattleMap.combatants) {
			c.rollinitiative();
			c.lastrefresh = Float.MIN_VALUE;
		}

	}

	public static GeneretadFight generatefight(final int el) {
		int delta = 0;
		GeneretadFight generated = null;
		while (generated == null) {
			generated = chooseopponents(el - delta);
			if (generated != null) {
				break;
			}
			if (delta != 0) {
				generated = chooseopponents(el + delta);
			}
			delta += 1;
		}
		return generated;
	}

	public static void copyoriginal() {
		Javelin.app.originalteam = new ArrayList<Combatant>();
		for (final Combatant m : BattleMap.blueTeam) {
			Javelin.app.originalteam.add(m.clone());
		}
		Javelin.app.originalfoes = new ArrayList<Combatant>();
		for (final Combatant m : BattleMap.redTeam) {
			Javelin.app.originalfoes.add(m.clone());
		}
	}

	static public GeneretadFight chooseopponents(final int el) {
		final HashSet<Monster> monsters = new HashSet<Monster>();
		for (final Combatant m : BattleMap.blueTeam) {
			monsters.add(m.source);
		}
		final ArrayList<Monster> opponents;
		try {
			opponents = EncounterGenerator.generate(el, Weather.now == 2);
		} catch (final GaveUpException e) {
			return null;
		}
		for (final Monster m : opponents) {
			monsters.add(m);
		}
		return new GeneretadFight(opponents);
	}

	/** TODO "dark tower" doesn't work - try to fix it? */
	static final String[] MAPTYPES = new String[] { "ruin", "graveyard",
			"goblin village", "dark forest", "caves" };

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

	public static void selectsquad() {
		/* Start first squad in the morning */
		Squad.active = new Squad(0, 0, 8);
		Squad.active.members = BattleMap.blueTeam;
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		// candidates.addAll(Javelin.MONSTERS.get(1f));
		candidates.addAll(Javelin.MONSTERS.get(1.25f));
		for (Monster m : new SquadScreen(candidates).select()) {
			Combatant c = Javelin.recruit(m);
			c.hp = c.source.hd.maximize();
			c.maxhp = c.hp;
		}
	}

	/**
	 * TODO refactor into class
	 */
	static public void namingscreen(final Monster m) {
		final String nametext = "Give a name to your " + m.name
				+ "! Press BACKSPACE to erase\n\n";
		final IntroScreen namescreen = new IntroScreen(nametext);
		String name = "";
		char f;
		naming: while (true) {
			f = IntroScreen.feedback();
			if (f == '\n') {
				if (!name.isEmpty()) {
					for (final Squad s : Squad.squads) {
						for (final Combatant namesake : s.members) {
							if (namesake.toString().equals(name)) {
								continue naming;
							}
						}
					}
					break;
				}
			}
			if (!(f == '\b' || f == ' ' || Character.isLetterOrDigit(f))) {
				continue;
			}
			if (f == '\b') {
				if (!name.isEmpty()) {
					name = name.substring(0, name.length() - 1);
				}
			} else {
				name = name + f;
			}
			namescreen.text = nametext + name;
			namescreen.repaint();
		}
		m.customName = name;
	}

	@Override
	public void preparebattlemap() {

	}

	@Override
	public void setupScreen() {
		// if (getScreen() == null) {
		// setScreen(OVERWORLD ? new WorldScreen(this)
		// : new BattleScreen(this));
		// } else {
		// // only need to reset the messages,
		// // otherwise we will start to
		// // leak memory/threads
		// Game.messagepanel = getScreen().messagepanel;
		// Game.messagepanel.clear();
		// }
		// switchScreen(getScreen());
	}
}