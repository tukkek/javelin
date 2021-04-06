package javelin.controller.db;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.content.action.world.meta.OpenJournal;
import javelin.controller.content.event.EventDealer;
import javelin.controller.content.event.urban.UrbanEvents;
import javelin.controller.content.event.wild.WildEvents;
import javelin.controller.content.wish.Ressurect;
import javelin.model.Miniatures;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * Saves and loads game progress to a file.
 *
 * TODO would be nice to have a backup save every half an hour or so...?
 *
 * @author alex
 */
public class StateManager{
	static class SaveThread extends Thread{
		File to;

		SaveThread(File to){
			this.to=to;
		}

		@Override
		public void run(){
			try(ObjectOutputStream writer=new ObjectOutputStream(
					new FileOutputStream(to))){
				if(WorldScreen.current!=null) WorldScreen.current.savediscovered();
				writer.writeBoolean(abandoned);
				writer.writeObject(World.seed);
				writer.writeObject(Dungeon.active);
				writer.writeObject(Incursion.currentel);
				writer.writeObject(Weather.current);
				writer.writeObject(Ressurect.dead);
				writer.writeObject(Season.current);
				writer.writeObject(Season.endsat);
				writer.writeObject(OpenJournal.content);
				writer.writeObject(WildEvents.instance);
				writer.writeObject(UrbanEvents.instance);
				writer.writeObject(Miniatures.miniatures);
				writer.flush();
				writer.close();
			}catch(final IOException e){
				throw new RuntimeException(e);
			}
			if(to==SAVEFILE) backup(false);
		}

		void hold(){
			try{
				join();
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Always called on normal exit. Saves a backup.
	 */
	public static final WindowAdapter SAVEONCLOSE=new WindowAdapter(){
		@Override
		public void windowClosing(WindowEvent e){
			Window w=e.getWindow();
			try{
				boolean inbattle=BattleScreen.active!=null
						&&!(BattleScreen.active instanceof WorldScreen);
				String warning="Exiting during battle will not save your progress.\n"
						+"Leave the game anyway?";
				if(inbattle&&JOptionPane.showConfirmDialog(w,warning,"Warning!",
						JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)
					return;
				w.dispose();
				if(BattleScreen.active!=null&&BattleScreen.active==WorldScreen.current){
					save(true).ifPresent(t->t.hold());
					backup(true).ifPresent(t->t.hold());
				}
				System.exit(0);
			}catch(RuntimeException exception){
				w.dispose();
				Javelin.app.uncaughtException(Thread.currentThread(),exception);
				System.exit(0);
			}
		}
	};

	static final String SAVEFOLDER=System.getProperty("user.dir");
	static final File BACKUPFOLDER=new File(SAVEFOLDER,"backup");
	static final File SAVEFILE=new File(SAVEFOLDER,
			World.scenario.getsaveprefix()+".save");
	static final int MINUTE=60*1000;
	public static boolean abandoned=false;
	public static boolean nofile=false;

	static long lastsave=System.currentTimeMillis();
	static long lastbackup=System.currentTimeMillis();

	static synchronized Optional<SaveThread> save(boolean force,File to){
		long now=System.currentTimeMillis();
		if(!force){
			if(now-lastsave<Preferences.saveinterval*MINUTE) return Optional.empty();
			if(Squad.active==null) return Optional.empty();
		}
		lastsave=now;
		var t=new SaveThread(to);
		t.start();
		return Optional.of(t);
	}

	/**
	 * Loads {@link #SAVEFILE}.
	 *
	 * @return <code>false</code> if starting a new game (no previous save).
	 */
	public static boolean load(){
		if(!SAVEFILE.exists()){
			nofile=true;
			return false;
		}
		try{
			final FileInputStream filestream=new FileInputStream(SAVEFILE);
			final ObjectInputStream stream=new ObjectInputStream(filestream);
			abandoned=stream.readBoolean();
			if(abandoned){
				abandoned=false;
				stream.close();
				return false;
			}
			World.seed=(World)stream.readObject();
			Javelin.act();
			for(ArrayList<Actor> instances:World.getseed().actors.values())
				for(Actor p:instances)
					p.place();
			Dungeon.active=(DungeonFloor)stream.readObject();
			Incursion.currentel=(Integer)stream.readObject();
			Weather.read((Integer)stream.readObject());
			Ressurect.dead=(Combatant)stream.readObject();
			Season.current=(Season)stream.readObject();
			Season.endsat=(Integer)stream.readObject();
			OpenJournal.content=(String)stream.readObject();
			WildEvents.instance=(EventDealer)stream.readObject();
			UrbanEvents.instance=(UrbanEvents)stream.readObject();
			Miniatures.miniatures=(ArrayList<Monster>)stream.readObject();
			stream.close();
			filestream.close();
			return true;
		}catch(final Throwable e){
			StateManager.clear();
			Javelin.app.uncaughtException(Thread.currentThread(),e);
			//			System.exit(20140406);
			return false;
		}
	}

	static Optional<SaveThread> backup(boolean force){
		if(Preferences.backupinterval==0) return Optional.empty();
		var now=Calendar.getInstance();
		if(!force
				&&now.getTimeInMillis()-lastbackup<Preferences.backupinterval*MINUTE)
			return null;
		lastbackup=now.getTimeInMillis();
		var timestamp="";
		timestamp+=now.get(Calendar.YEAR)+"-";
		timestamp+=format(now.get(Calendar.MONTH)+1)+"-";
		timestamp+=format(now.get(Calendar.DAY_OF_MONTH))+"-";
		timestamp+=format(now.get(Calendar.HOUR_OF_DAY))+".";
		timestamp+=format(now.get(Calendar.MINUTE))+".";
		timestamp+=format(now.get(Calendar.SECOND));
		var prefix=World.scenario.getsaveprefix();
		BACKUPFOLDER.mkdir();
		var backup=new File(BACKUPFOLDER,prefix+"-"+timestamp+".save");
		return save(true,backup);
	}

	static String format(int i){
		return i>=10?String.valueOf(i):"0"+i;
	}

	/**
	 * For some reason delete() doesn't work on all systems. The field 'abandon'
	 * should take care of any uncleared files.
	 */
	public static void clear(){
		abandoned=true;
		save(true).ifPresent(t->t.hold());
	}

	/**
	 * This should only be called from one place during normal execution of the
	 * game! Saving can be a slow process, especially on late game and very
	 * error-prone if not done carefully! Any error could potentially represent
	 * the loss of dozens of hours of gameplay so don't call this method unless
	 * absolutely necessary!
	 *
	 * @param force If <code>false</code> will only save according to
	 *          {@link Preferences#saveinterval}.
	 * @return The saving operation or <code>null</code>.
	 */
	public static Optional<SaveThread> save(boolean force){
		return save(force,SAVEFILE);
	}
}
