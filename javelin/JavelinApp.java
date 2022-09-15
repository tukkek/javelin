package javelin;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javelin.controller.TextReader;
import javelin.controller.ai.ThreadManager;
import javelin.controller.content.ContentSummary;
import javelin.controller.content.fight.Fight;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.WorldGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.old.QuestApp;
import javelin.old.RPG;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;

/**
 * Application and game life-cycle.
 *
 * TODO refactor a time manager out of this which works with
 * TimeManager#tick(int time,WorldState world)?
 *
 * @author alex
 */
public class JavelinApp extends QuestApp implements UncaughtExceptionHandler{
  /** Lower-case operating system name. */
  public static final String SYSTEM=System.getProperty("os.name").toLowerCase();

  static final String CRASHMESSAGE="""
      \n\nUnfortunately an error ocurred.
      Please send a screenshot of the next message or the log file (error.txt)
      to one of the channels below, to help get it fixed on future releases.\n
      Post to reddit -- http://www.reddit.com/r/javelinrl
      Leave a comment on the blog -- http://javelinrl.wordpress.com
      Or write an e-mail -- javelinrl@gmail.com\n
      Thank you for your patience and support!\n
      If for any reason your current game fails to load when you restart Javelin,
      you can find backups on the "backup" folder. Just copy one of them to the
      main folder and rename it to "campaign.save" to restore your progress.\n
      """;
  static final String[] CRASHQUOTES={"A wild error appears!",
      "You were eaten by a grue.","So again it has come to pass...",
      "Mamma mia!",};

  /** TODO change into actual context (separate from UI code). */
  public static WorldScreen context;

  static String version="Version: ?";

  /** Root window. */
  public JFrame frame;

  @Override
  public void run(){
    Thread.setDefaultUncaughtExceptionHandler(this);
    Preferences.setup();// pre
    initialize();
    if(!StateManager.load()){
      if(StateManager.nofile) disclaimer();
      startcampaign();
    }
    Preferences.setup();// post
    preparedebug();
    if(Javelin.DEBUG) while(true) loop();
    while(true) try{
      loop();
    }catch(RuntimeException e){
      uncaughtException(Thread.currentThread(),e);
    }
  }

  void loop(){
    try{
      if(Dungeon.active==null) context=new WorldScreen(true);
      else Dungeon.active.enter();
      while(true){
        Javelin.app.switchScreen(context);
        JavelinApp.context.turn();
      }
    }catch(StartBattle e){
      StateManager.save(true);
      if(Debug.disablecombat) return;
      Fight.current=e.fight;
      try{
        e.battle();
      }catch(EndBattle end){
        EndBattle.end();
      }
      Fight.current=null;
      Fight.state=null;
    }
  }

  /** @return A pretty-printed stack trace. */
  public static String printstacktrace(Throwable e){
    var error=e.getMessage()+" ("+e.getClass()+")";
    error+="\n";
    for(StackTraceElement stack:e.getStackTrace()) error+=stack.toString()+"\n";
    return error;
  }

  void disclaimer(){
    while(TextReader
        .show(new File("README.txt"),
            "This message will only be shown once, press ENTER to continue.")
        .getKeyCode()!=KeyEvent.VK_ENTER){
      // wait for enter
    }
  }

  static void initialize(){
    var readme=TextReader.read(new File("README.txt"));
    System.out.println(readme.replaceAll("\n\n","\n"));
    try{
      version=TextReader.read(new File("doc","VERSION.txt"));
    }catch(RuntimeException e){
      //file is generated, might not be there at all
    }
    ThreadManager.determineprocessors();
  }

  void startcampaign(){
    WorldGenerator.build();
    var start=WorldGenerator.start;
    var s=new SquadScreen().open();
    s.setlocation(start.getlocation());
    s.displace();
    s.place();
    if(start instanceof Town t) s.lasttown=t;
    Squad.active=s;
    if(Javelin.DEBUG){
      new ContentSummary().generate();
      Debug.oncampaignstart();
      StateManager.save(true).get().hold();
    }
  }

  void preparedebug(){
    if(Debug.gold!=null) Squad.active.gold=Debug.gold;
    if(Debug.xp!=null) for(final Combatant m:Squad.active.members)
      m.xp=new BigDecimal(Debug.xp/100f);
  }

  @Override
  public void uncaughtException(Thread thread,Throwable e){
    if(Thread.currentThread()!=thread) return; //no sub-thread dialogs
    var quote=CRASHQUOTES[RPG.r(CRASHQUOTES.length)];
    JOptionPane.showMessageDialog(Javelin.app,quote+CRASHMESSAGE);
    if(!version.endsWith("\n")) version+="\n";
    System.err.print(version);
    e.printStackTrace();
    var properties=List.of("os.name","os.version","os.arch").stream()
        .map(System::getProperty).collect(Collectors.joining(" "));
    properties=String.format("System: %s\n",properties);
    var message=version+properties+"\n"+printstacktrace(e);
    var errors=new HashSet<Throwable>(2);
    while(e.getCause()!=null&&errors.add(e)){
      e=e.getCause();
      message+=System.lineSeparator()+printstacktrace(e);
    }
    try{
      Preferences.write(message,"error.txt");
    }catch(IOException e1){
      // ignore
    }
    JOptionPane.showMessageDialog(Javelin.app,message);
    System.exit(20201202);
  }
}
