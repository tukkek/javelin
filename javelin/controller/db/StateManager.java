package javelin.controller.db;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.world.Journal;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.Realm;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Saves and loads game progress to a file.
 * 
 * @author alex
 */
public class StateManager {

	private static final File SAVEFILE =
			new File(System.getProperty("user.dir"), "javelin.save");
	private static final Thread SAVEONEXIT = new Thread(new Runnable() {
		@Override
		public void run() {
			if (WorldScreen.active == null) {
				// quit before stating a game
				return;
			}
			if (BattleScreen.active != null) {
				// don't save during battle
				return;
			}
			while (!SAVEFILE.canWrite()) {
				System.out.println("Waiting for save to finish...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// should not happen
				}
			}
			save();
		}
	});
	public static boolean abandoned = false;
	static public boolean nofile = false;

	static {
		Runtime.getRuntime().addShutdownHook(SAVEONEXIT);

	}

	public static synchronized void save() {
		try {
			ObjectOutputStream writer =
					new ObjectOutputStream(new FileOutputStream(SAVEFILE));
			writer.writeBoolean(abandoned);
			for (final WorldActor a : Squad.getall(Squad.class)) {
				Squad s = (Squad) a;
				s.x = s.visual.x;
				s.y = s.visual.y;
			}
			writer.writeObject(WorldActor.INSTANCES);
			writer.writeObject(World.seed);
			writer.writeObject(Dungeon.active);
			writer.writeObject(Incursion.currentel);
			writer.writeObject(Weather.current);
			writer.writeObject(EndBattle.lastkilled);
			writer.writeObject(WorldScreen.discovered);
			writer.writeObject(World.roads);
			writer.writeObject(World.highways);
			writer.writeObject(Season.current);
			writer.writeObject(Season.endsat);
			writer.writeObject(Key.queue);
			writer.writeObject(Journal.content);
			writer.flush();
			writer.close();
		} catch (final NotSerializableException e) {
			if (Javelin.DEBUG) {
				/* fail when it's debug */
				throw new RuntimeException(e);
			}
			/* fail gracefully, hopefully the next invocation will work fine */
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean load() {
		if (!SAVEFILE.exists()) {
			nofile = true;
			return false;
		}
		try {
			final FileInputStream filestream = new FileInputStream(SAVEFILE);
			final ObjectInputStream stream = new ObjectInputStream(filestream);
			abandoned = stream.readBoolean();
			if (abandoned) {
				abandoned = false;
				stream.close();
				return false;
			}
			WorldActor.INSTANCES =
					(HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>>) stream
							.readObject();
			Javelin.act();
			World.seed = (World) stream.readObject();
			JavelinApp.overviewmap = WorldScreen.worldmap;
			for (ArrayList<WorldActor> instances : WorldActor.INSTANCES
					.values()) {
				for (WorldActor p : instances) {
					p.place();
				}
			}
			Haxor.singleton =
					(Haxor) WorldActor.INSTANCES.get(Haxor.class).get(0);
			Haxor.singleton.place();
			Dungeon.active = (Dungeon) stream.readObject();
			Incursion.currentel = (Integer) stream.readObject();
			Weather.read((Integer) stream.readObject());
			EndBattle.lastkilled = (Combatant) stream.readObject();
			WorldScreen.discovered = (HashSet<Point>) stream.readObject();
			World.roads = (boolean[][]) stream.readObject();
			World.highways = (boolean[][]) stream.readObject();
			Season.current = (Season) stream.readObject();
			Season.endsat = (Integer) stream.readObject();
			Key.queue = (LinkedList<Realm>) stream.readObject();
			Journal.content = (String) stream.readObject();
			stream.close();
			return true;
		} catch (final Throwable e1) {
			e1.printStackTrace(System.out);
			System.out.println("Your save game could not be loaded\n"
					+ "It has been deleted so you can restart the game with a new save\n"
					+ "If this is happening constantly please inform us of the error message above");
			StateManager.clear();
			if (!Javelin.DEBUG) {
				Window.getWindows()[0].dispose();
				System.out.println("\nPress any key to exit...");
				// Game.getInput();
				try {
					System.in.read();
				} catch (IOException e) {
					// die gracefully
				}
			}
			Runtime.getRuntime().removeShutdownHook(StateManager.SAVEONEXIT);
			System.exit(20140406);
			return false;
		}
	}

	/**
	 * For some reason delete() doesn't work on all systems. The field 'abandon'
	 * should take care of any uncleared files.
	 */
	public static void clear() {
		abandoned = true;
		save();
	}

}
