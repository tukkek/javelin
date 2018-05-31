package javelin.controller.db;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.action.world.OpenJournal;
import javelin.controller.wish.Ressurect;
import javelin.model.unit.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Saves and loads game progress to a file.
 *
 * @author alex
 */
public class StateManager {
	public static final String SAVEFOLDER = System.getProperty("user.dir");
	/** Normal save file. */
	public static final File SAVEFILE = new File(SAVEFOLDER,
			World.scenario.getsaveprefix() + ".save");

	/**
	 * Always called on normal exit.
	 */
	public static final WindowAdapter SAVEONCLOSE = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			try {
				boolean inbattle = BattleScreen.active != null
						&& !(BattleScreen.active instanceof WorldScreen);
				String warning = "Exiting during battle will not save your progress.\n"
						+ "Leave the game anyway?";
				if (inbattle && JOptionPane.showConfirmDialog(w, warning,
						"Warning!",
						JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
					return;
				}
				w.dispose();
				if (BattleScreen.active != null
						&& BattleScreen.active == WorldScreen.current) {
					save(true, SAVEFILE);
				}
				System.exit(0);
			} catch (RuntimeException exception) {
				w.dispose();
				JavelinApp.handlefatalexception(exception);
				System.exit(0);
			}
		};
	};

	static final int MINUTE = 60 * 1000;

	public static boolean abandoned = false;
	public static boolean nofile = false;
	static long lastsave = System.currentTimeMillis();

	/**
	 * This should only be called from one place during normal execution of the
	 * game! Saving can be a slow process, especially on late game and very
	 * error-prone if not done carefully! Any error could potentially represent
	 * the loss of dozens of hours of gameplay so don't call this method unless
	 * absolutely necessary!
	 *
	 * @param force
	 *            If <code>false</code> will only save once upon a certain
	 *            number of calls.
	 * @param to
	 */
	public static synchronized void save(boolean force, File to) {
		long now = System.currentTimeMillis();
		if (!force && now - lastsave < Preferences.SAVEINTERVAL * MINUTE) {
			return;
		}
		lastsave = now;
		try {
			ObjectOutputStream writer = new ObjectOutputStream(
					new FileOutputStream(to));
			if (WorldScreen.current != null) {
				WorldScreen.current.savediscovered();
			}
			writer.writeBoolean(abandoned);
			writer.writeObject(World.seed);
			writer.writeObject(Dungeon.active);
			writer.writeObject(Incursion.currentel);
			writer.writeObject(Weather.current);
			writer.writeObject(Ressurect.dead);
			writer.writeObject(Season.current);
			writer.writeObject(Season.endsat);
			writer.writeObject(OpenJournal.content);
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

	/**
	 * Loads {@link #SAVEFILE} and saves a backup of it.
	 *
	 * @return <code>false</code> if starting a new game (no previous save).
	 */
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
			World.seed = (World) stream.readObject();
			Javelin.act();
			for (ArrayList<Actor> instances : World.getseed().actors.values()) {
				for (Actor p : instances) {
					p.place();
				}
			}
			Dungeon.active = (Dungeon) stream.readObject();
			Incursion.currentel = (Integer) stream.readObject();
			Weather.read((Integer) stream.readObject());
			Ressurect.dead = (Combatant) stream.readObject();
			Season.current = (Season) stream.readObject();
			Season.endsat = (Integer) stream.readObject();
			OpenJournal.content = (String) stream.readObject();
			stream.close();
			filestream.close();
			backup();
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
			System.exit(20140406);
			return false;
		}
	}

	private static void backup() {
		if (!Preferences.BACKUP) {
			return;
		}
		Calendar now = Calendar.getInstance();
		String timestamp = "";
		timestamp += now.get(Calendar.YEAR) + "-";
		timestamp += format(now.get(Calendar.MONTH) + 1) + "-";
		timestamp += format(now.get(Calendar.DAY_OF_MONTH)) + "-";
		timestamp += format(now.get(Calendar.HOUR_OF_DAY)) + ".";
		timestamp += format(now.get(Calendar.MINUTE)) + ".";
		timestamp += format(now.get(Calendar.SECOND));
		File folder = new File(SAVEFOLDER, "backup");
		folder.mkdir();
		File backup = new File(folder,
				World.scenario.getsaveprefix() + "-" + timestamp + ".save");
		save(true, backup);
	}

	private static String format(int i) {
		return i >= 10 ? String.valueOf(i) : "0" + i;
	}

	/**
	 * For some reason delete() doesn't work on all systems. The field 'abandon'
	 * should take care of any uncleared files.
	 */
	public static void clear() {
		abandoned = true;
		save(true, StateManager.SAVEFILE);
	}

}
