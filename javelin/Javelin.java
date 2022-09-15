package javelin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.MediaTracker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import javelin.controller.collection.CountingSet;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.fields.Organization;
import javelin.model.item.Item;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.Actor;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.old.Interface;
import javelin.old.messagepanel.MessagePanel;
import javelin.old.messagepanel.TextZone;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Utility class for broad-level rules and game-behavior. Add the VM argument
 * -Ddebug=true to the java command line for easier debugging and logging.
 *
 * @see #DEBUG
 * @author alex
 */
public class Javelin{
  public enum Delay{
    NONE,WAIT,BLOCK
  }

  /**
   * Add -Ddebug=true to the java VM command line for easier debugging and
   * logging.
   */
  public static final boolean DEBUG=System.getProperty("debug")!=null;

  /** Roll target on a d20. */
  public static final int HARD=16;
  /** Roll target on a d20. */
  public static final int FAIR=12;
  /** Roll target on a d20. */
  public static final int EASY=8;
  /** Roll target on a d20. */
  public static final int EFFORTLESS=4;

  static final String TITLE="Javelin";
  static final DecimalFormat COSTFORMAT=new DecimalFormat("####,###,##0");

  /** Singleton. */
  public static JavelinApp app;
  public static boolean delayblock=false;
  /** Not sure how necessary this is - can do away with on 2.0+ TODO */
  public static MediaTracker tracker;

  static{
    ClassLevelUpgrade.setup();
    Spell.setup();
    try{
      final var monsterdb=new MonsterReader();
      final var xml=SAXParserFactory.newInstance().newSAXParser()
          .getXMLReader();
      xml.setContentHandler(monsterdb);
      xml.setErrorHandler(monsterdb);
      var filereader=new FileReader("monsters.xml");
      xml.parse(new InputSource(filereader));
      filereader.close();
    }catch(final Exception e){
      throw new RuntimeException(e);
    }
    Organization.setup();
    MonsterReader.closelogs();
    Summon.setupsummons();
    Spell.setup();
    Gear.setup();
    Item.setup();
    Debug.oninit();
  }

  /**
   * First method to be called.
   *
   * @param args See {@link #DEBUG}.
   */
  public static void main(final String[] args){
    Thread.currentThread().setName("Javelin");
    final var f=new JFrame(TITLE);
    tracker=new MediaTracker(f);
    app=new JavelinApp();
    f.setExtendedState(f.getExtendedState()|Frame.MAXIMIZED_BOTH);
    f.setBackground(java.awt.Color.black);
    f.addWindowListener(StateManager.SAVEONCLOSE);
    f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    f.setLayout(new BorderLayout());
    f.setFocusTraversalKeysEnabled(false);
    f.setIconImages(Arrays.asList(Images.ICONS));
    app.frame=f;
    app.setVisible(false);
    f.add(app);
    f.setSize(app.getPreferredSize().width,app.getPreferredSize().height);
    f.addKeyListener(new KeyAdapter(){
      @Override
      public void keyPressed(final KeyEvent e){
        Interface.userinterface.go(e);
      }
    });
    app.setVisible(true);
    f.setVisible(true);
    f.setExtendedState(Frame.MAXIMIZED_BOTH);
    app.init();
  }

  /**
   * This is also the only function that should write to {@link Squad#active} so
   * it should be called with extreme caution to avoid changing it in the middle
   * of an action.
   *
   * @return Next squad to act.
   */
  public static Squad act(){
    var next=nexttoact();
    Squad.active=next;
    if(WorldScreen.lastday==-1)
      WorldScreen.lastday=Math.ceil(Period.gettime()/24.0);
    return next;
  }

  /**
   * @return Next squad to act.
   * @see Squad#time
   */
  public static Squad nexttoact(){
    Squad next=null;
    for(var s:Squad.getsquads()){
      if(s.gettime()<(WorldScreen.lastday-1)*24) continue;
      if(next==null||s.gettime()<next.gettime()) next=s;
    }
    return next;
  }

  /**
   * Pure fluff/flavor.
   *
   * @return Welcomes the playet to the game based on the current time of the
   *   day.
   */
  public static String welcome(){
    String flavor;
    if(Period.MORNING.is()) flavor="What dangers lie ahead..?";
    else if(Period.AFTERNOON.is()) flavor="Onwards to victory!";
    else if(Period.EVENING.is()) flavor="Cheers!";
    else if(Period.NIGHT.is())
      flavor="What a horrible night to suffer an invasion...";
    else throw new RuntimeException("No welcome message");
    return "Welcome! "+flavor
        +"\n\n(press h at the overworld or battle screens for help)";
  }

  /**
   * Once the player has no more {@link Squad}s and {@link Combatant}s under his
   * control call this to stop the current game, invalidate the save file,
   * record the highscore and exit the application.
   */
  public static boolean lose(){
    if(!Squad.getsquads().isEmpty()
        ||World.getactors().stream().filter(Actor::hold).findAny().isPresent())
      return false;
    Javelin.app.switchScreen(BattleScreen.active);
    BattleScreen.active.messagepanel.clear();
    message("Please wait...",Delay.NONE);
    StateManager.clear();
    message("You have lost all your units! Game over T_T",true);
    System.exit(0);
    return true;
  }

  /**
   * @param rolltohit The number that needs to be rolled on a d20 for this
   *   action to succeed.
   * @return A textual representation of how easy or hard this action is to
   *   achieve.
   */
  static public String getchance(int rolltohit){
    if(rolltohit<=EFFORTLESS) return "effortless";
    if(rolltohit<=EASY) return "easy";
    if(rolltohit<=FAIR) return "fair";
    if(rolltohit<=HARD) return "hard";
    return "unlikely";
  }

  /**
   * Utility function for user-input selection.
   *
   * TODO due to the UI being poor, if this method runs out of alphanumeric
   * characters to use, it will display more choices but not allow the player to
   * actually select them.
   *
   * @param prompt Text to show the user.
   * @param names Will show each's {@link Object#toString()} as an option.
   * @param fullscreen <code>true</code> to open in a new screen. Otherwise uses
   *   the message panel.
   * @param forceselection If <code>false</code> will allow the user to abort
   *   the operation.
   * @return The index of the selected element or -1 if aborted. Won't return an
   *   invalid index other than -1.
   */
  static public int choose(String prompt,List<?> names,boolean fullscreen,
      boolean forceselection){
    if(!forceselection) prompt+=" (q to quit)";
    prompt+="\n\n";
    var nnames=names.size();
    var multicolumn=nnames>20;
    var options=new ArrayList<>();
    for(var i=0;i<nnames;i++){
      var leftcolumn=i%2==0;
      var name=names.get(i).toString();
      options.add(name);
      var item="["+SelectScreen.getkey(i)+"] "+name;
      if(multicolumn&&leftcolumn) while(item.length()<50) item+=" ";
      prompt+=item;
      if(!multicolumn||!leftcolumn) prompt+="\n";
    }
    if(fullscreen) app.switchScreen(new InfoScreen(prompt));
    else{
      app.switchScreen(BattleScreen.active);
      MessagePanel.active.clear();
      Javelin.message(prompt,Delay.NONE);
    }
    try{
      while(true) try{
        var c=InfoScreen.feedback();
        if(!forceselection&&c==InfoScreen.ESCAPE) return -1;
        var selected=SelectScreen.convertkeytoindex(c);
        if(0<=selected&&selected<names.size()) return selected;
      }catch(Exception e){
        continue;
      }
    }finally{
      if(fullscreen) app.switchScreen(BattleScreen.active);
    }
  }

  /**
   * Main output function for {@link WorldScreen}. Waits for user input for
   * confirmation.
   *
   * @param text Prints this message in the status panel.
   * @param requireenter If <code>true</code> will wait for the player to press
   *   ENTER, otherwise any key will do.
   * @return the key pressed by the user as confirmation for seeing the message.
   */
  public static KeyEvent message(String text,boolean requireenter){
    var screen=BattleScreen.active==null?WorldScreen.current
        :BattleScreen.active;
    app.switchScreen(screen);
    screen.center();
    MessagePanel.active.clear();
    text+="\nPress "+(requireenter?"ENTER":"any key")+" to continue...";
    Javelin.message(text,Delay.NONE);
    var input=Javelin.input();
    while(requireenter&&input.getKeyChar()!='\n') input=Javelin.input();
    MessagePanel.active.clear();
    return input;
  }

  /**
   * Prompts a message in the {@link WorldScreen}.
   *
   * @param prompt Text to show.
   * @return Any {@link InfoScreen#feedback()}.
   */
  static public Character prompt(final String prompt,boolean center){
    MessagePanel.active.clear();
    Javelin.message(prompt,Delay.NONE);
    if(center) BattleScreen.active.center();
    return InfoScreen.feedback();
  }

  /**
   * @param prompt Shows this message...
   * @return and returns the user input.
   */
  static public Character prompt(final String prompt){
    return prompt(prompt,false);
  }

  public static String describedifficulty(int dc){
    if(dc<=0) return "very easy";
    if(dc<=5) return "easy";
    if(dc<=10) return "average";
    if(dc<=15) return "tough";
    if(dc<=20) return "challenging";
    if(dc<=25) return "formidable";
    if(dc<=30) return "heroic";
    return "nearly impossible";
  }

  /** @param message Shows this in a fullscreen, requires Enter to leave. */
  public static void show(String message){
    var s=new InfoScreen("");
    s.print(message);
    while(s.getinput()!='\n'){}
  }

  /** As {@link #prompt(String)} but full-screen. */
  public static char promptscreen(String prompt){
    var s=new InfoScreen("");
    s.print(prompt);
    return InfoScreen.feedback();
  }

  /** @return Textual representation of the givne {@link Option#price}. */
  static public String format(double gold){
    return COSTFORMAT.format(gold);
  }

  /**
   * Example: 194,151 will be "rounded" to 190,000.
   *
   * A 10% loss of precision is expected at most but it tends to normalize (and
   * lessen with the game's exponential increase as challenge levels rise). The
   * loss of precision is easily worth the much higher readability.
   *
   * @param gold A value in gold or other unit that needn't be precise.
   *
   * @return The input value, but rounded off as to be more legible.
   */
  static public int round(double gold){
    if(gold<=100) return (int)Math.round(gold);
    var roundto=1;
    while(roundto*100<gold) roundto=roundto*10;
    return roundto*(int)Math.round(gold/roundto);
  }

  /** Updates the {@link MapPanel} and {@link MessagePanel}. */
  public static void redraw(){
    var b=BattleScreen.active;
    b.mappanel.refresh();
    if(b.statuspanel!=null) b.statuspanel.repaint();
    MessagePanel.active.repaint();
  }

  public static KeyEvent input(){
    if(MessagePanel.active!=null) MessagePanel.active.repaint();
    Interface.userinterface.getinput();
    return Interface.userinterface.keyevent;
  }

  /**
   * Main output function for {@link BattleScreen}s.
   *
   * @param message Text to be printed.
   * @param t TODO remove
   * @param See {@link Delay}.
   */
  public static void message(String out,Delay d){
    if(BattleScreen.lastaicheck==null&&d==Delay.BLOCK) d=Delay.WAIT;
    MessagePanel.active.add(out);
    switch(d){
      case WAIT:
        try{
          redraw();
          Thread.sleep(Preferences.messagewait);
        }catch(final InterruptedException e){
          e.printStackTrace();
        }
        MessagePanel.active.clear();
        break;
      case BLOCK:
        MessagePanel.active.add("\n"+TextZone.BLACK+"-- ENTER --");
        delayblock=true;
        break;
    }
  }

  /**
   * Calls {@link #prompt(String)}p until the user presses one of the allowed
   * {@link Character}s.
   *
   * @param allowed <code>null</code> should never be included.
   * @return User input, guaranteed to be among the allowed ones.
   */
  public static Character prompt(String prompt,Set<Character> allowed){
    Character input=null;
    while(input==null||!allowed.contains(input)) input=prompt(prompt);
    BattleScreen.active.messagepanel.clear();
    return input;
  }

  public static String group(List<?> foes){
    var count=new CountingSet();
    count.casesensitive=true;
    for(Object c:foes) count.add(c.toString());
    return count.toString();
  }

  /** @return A "Capitalized" version of the input. */
  public static String capitalize(String s){
    return Character.toUpperCase(s.charAt(0))+s.substring(1).toLowerCase();
  }

  /**
   * @return Given DC and the d20 roll bonus, human-friendly chance of success.
   */
  static public String describe(int bonus,int dc){
    var difficulty=dc-bonus;
    if(difficulty<=00) return "very easy";
    if(difficulty<=05) return "easy";
    if(difficulty<=10) return "fair";
    if(difficulty<=15) return "tough";
    if(difficulty<=20) return "formidable";
    return "impossible";
  }
}
