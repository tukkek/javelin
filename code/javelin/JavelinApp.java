package javelin;

import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;

import javax.swing.JFrame;

import javelin.controller.Report;
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
import javelin.model.world.location.town.Town;
import javelin.old.QuestApp;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Screen;
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
public class JavelinApp extends QuestApp{
  /** Lower-case operating system name. */
  public static final String SYSTEM=System.getProperty("os.name").toLowerCase();

  static final String README="readme.md";

  /** TODO change into actual context (separate from UI code). */
  public static WorldScreen context;
  /// Read from autobuilt `doc/VERSION.txt` if present.
  public static String version="";

  /** Root window. */
  public JFrame frame;

  @Override
  public void run(){
    if(!Javelin.DEBUG)
      Thread.setDefaultUncaughtExceptionHandler(Report.INSTANCE);
    Preferences.setup();// pre
    initialize();
    if(!StateManager.load()){
      if(StateManager.nofile) disclaimer();
      startcampaign();
    }
    Preferences.setup();// post
    preparedebug();
    while(true) try{
      loop();
    }catch(RuntimeException e){
      Report.handle(e);
    }
  }

  void fight(StartBattle exception){
    if(Debug.disablecombat) return;
    var f=exception.fight;
    if(f.skip()) return;
    Fight.current=f;
    try{
      exception.battle();
    }catch(EndBattle end){
      EndBattle.end();
    }
    Fight.current=null;
    Fight.state=null;
  }

  void loop(){
    try{
      if(BattleScreen.active==null) Screen.reopen();
      while(true) JavelinApp.context.turn();
    }catch(StartBattle e){
      fight(e);
    }
  }

  void disclaimer(){
    var extra="This message will only be shown once, press ENTER to continue.";
    var enter=KeyEvent.VK_ENTER;
    while(TextReader.show(new File(README),extra).getKeyCode()!=enter){
      // wait for enter
    }
  }

  static void initialize(){
    var readme=TextReader.read(new File(README));
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
    s.gold=200;
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

}
