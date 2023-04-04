package javelin.controller.db;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
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
import javelin.model.unit.Combatant;
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
 * Saves game and backups using an independent {@link Thread}. The current save
 * strategy is:
 *
 * 1. Will always force a save when entering battle. At this point the player
 * won't be interacting with the {@link World}, which means data won't be
 * modified while a save is in progress. This is the ideal scenario as it's
 * seamless to the player and happens in a background thread.
 *
 * 2. Will always check before a player acts in the {@link World} if the
 * auto-save timer has expired and if so, will trigger a save. This should be
 * rare, as the default auto-save interval is 10 minutes and forced saves while
 * the player is doing battle (#1) will naturally reset the timer as they occur.
 *
 * 3. In the rare case that a save happens outside of battle, the game will show
 * a message and {@link SaveThread#hold()} until the save is completed. While
 * relatively fast, this is annoying but should happen only rarely. Players are
 * always free to change the auto-save interval (or disable it entirely) to suit
 * their personal preferences as well.
 *
 * 4. Backup saves are made according to the configured interval (30 minutes by
 * default) and are triggered automatically as part of any save action
 * immediately after the main save is completed.
 *
 * 5. A save is always forced upon closing the game window - then a backup save
 * as well (unless backups are disabled entirely).
 *
 * @author alex
 */
public class StateManager{
  static final String PREFIX="campaign";

  /**
   * Parallel save.
   *
   * @author alex
   */
  public static class SaveThread extends Thread{
    File to;
    byte[] data;

    SaveThread(File to){
      this.to=to;
    }

    void prepare(){
      try(var bytes=new ByteArrayOutputStream();
          var s=new ObjectOutputStream(bytes);){
        if(WorldScreen.current!=null) WorldScreen.current.savediscovered();
        s.writeBoolean(abandoned);
        var squad=Squad.active;
        if(squad!=null){
          var squads=World.seed.actors.get(Squad.class);
          squads.remove(squad);
          squads.add(0,squad);
        }
        s.writeObject(World.seed);
        s.writeObject(Dungeon.active);
        s.writeObject(Incursion.currentel);
        s.writeObject(Weather.current);
        s.writeObject(Ressurect.dead);
        s.writeObject(Season.current);
        s.writeObject(Season.endsat);
        s.writeObject(OpenJournal.content);
        s.writeObject(WildEvents.instance);
        s.writeObject(UrbanEvents.instance);
        s.flush();
        bytes.flush();
        data=bytes.toByteArray();
      }catch(final IOException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public synchronized void run(){
      try{
        Files.write(to.toPath(),data);
        if(to==SAVEFILE) backup(false);
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }

    /** {@link #join()} and throws errors as {@link RuntimeException}. */
    public void hold(){
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
    public void windowClosing(WindowEvent event){
      var w=event.getWindow();
      try{
        var b=BattleScreen.active;
        var inbattle=b!=null&&!(b instanceof WorldScreen);
        var warning="Exiting during battle will not save your progress.\n"
            +"Leave the game anyway?";
        var options=JOptionPane.OK_CANCEL_OPTION;
        var ok=JOptionPane.OK_OPTION;
        if(inbattle
            &&JOptionPane.showConfirmDialog(w,warning,"Warning!",options)!=ok)
          return;
        w.dispose();
        if(b!=null&&b==WorldScreen.current){
          save(true).ifPresent(SaveThread::hold);
          backup(true);
        }
        System.exit(0);
      }catch(RuntimeException e){
        w.dispose();
        Javelin.app.uncaughtException(Thread.currentThread(),e);
        System.exit(0);
      }
    }
  };

  static final String SAVEFOLDER=System.getProperty("user.dir");
  static final File BACKUPFOLDER=new File(SAVEFOLDER,"backup");
  static final File SAVEFILE=new File(SAVEFOLDER,PREFIX+".save");
  static final int MINUTE=60*1000;
  public static boolean abandoned=false;
  public static boolean nofile=false;

  static long lastsave=System.currentTimeMillis();
  static long lastbackup=System.currentTimeMillis();
  static SaveThread current;

  static synchronized Optional<SaveThread> save(boolean force,File to){
    var now=System.currentTimeMillis();
    if(!force)
      if(now-lastsave<Preferences.saveinterval*MINUTE||Squad.active==null)
        return Optional.empty();
    if(current!=null) current.hold();
    lastsave=now;
    current=new SaveThread(to);
    current.prepare();
    current.start();
    //    t.hold();//TODO also restore preferences.properties
    return Optional.of(current);
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
      final var filestream=new FileInputStream(SAVEFILE);
      final var stream=new ObjectInputStream(filestream);
      abandoned=stream.readBoolean();
      if(abandoned){
        abandoned=false;
        stream.close();
        return false;
      }
      World.seed=(World)stream.readObject();
      Javelin.act();
      for(ArrayList<Actor> instances:World.getseed().actors.values())
        for(Actor p:instances) p.place();
      Squad.active=(Squad)World.seed.actors.get(Squad.class).get(0);
      Dungeon.active=(DungeonFloor)stream.readObject();
      Incursion.currentel=(Integer)stream.readObject();
      Weather.read((Integer)stream.readObject());
      Ressurect.dead=(Combatant)stream.readObject();
      Season.current=(Season)stream.readObject();
      Season.endsat=(Integer)stream.readObject();
      OpenJournal.content=(String)stream.readObject();
      WildEvents.instance=(EventDealer)stream.readObject();
      UrbanEvents.instance=(UrbanEvents)stream.readObject();
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

  static void backup(boolean force){
    if(Preferences.backupinterval==0) return;
    var now=Calendar.getInstance();
    var time=now.getTimeInMillis();
    if(!force&&time-lastbackup<Preferences.backupinterval*MINUTE) return;
    if(current!=Thread.currentThread()) current.hold();
    lastbackup=time;
    var timestamp="";
    timestamp+=now.get(Calendar.YEAR)+"-";
    timestamp+=format(now.get(Calendar.MONTH)+1)+"-";
    timestamp+=format(now.get(Calendar.DAY_OF_MONTH))+"-";
    timestamp+=format(now.get(Calendar.HOUR_OF_DAY))+".";
    timestamp+=format(now.get(Calendar.MINUTE))+".";
    timestamp+=format(now.get(Calendar.SECOND));
    BACKUPFOLDER.mkdir();
    var backup=new File(BACKUPFOLDER,"%s-%s.save".formatted(PREFIX,timestamp));
    try{
      Files.copy(SAVEFILE.toPath(),backup.toPath());
    }catch(IOException e){
      if(Javelin.DEBUG) throw new RuntimeException(e);
    }
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
    save(true).ifPresent(SaveThread::hold);
  }

  /**
   * This should only be called from one place during normal execution of the
   * game! Saving can be a slow process, especially on late game and very
   * error-prone if not done carefully! Any error could potentially represent
   * the loss of dozens of hours of gameplay so don't call this method unless
   * absolutely necessary!
   *
   * @param force If <code>false</code> will only save according to
   *   {@link Preferences#saveinterval}.
   * @return The saving operation or <code>null</code>.
   */
  public static Optional<SaveThread> save(boolean force){
    return save(force,SAVEFILE);
  }
}
