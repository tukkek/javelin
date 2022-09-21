package javelin.view.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Point;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.ThreadManager;
import javelin.controller.content.action.Action;
import javelin.controller.content.action.ActionCost;
import javelin.controller.content.action.ActionMapping;
import javelin.controller.content.action.Dig;
import javelin.controller.content.action.Examine;
import javelin.controller.content.action.Movement;
import javelin.controller.content.action.world.WorldAction;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Period;
import javelin.old.Interface;
import javelin.old.QuestApp;
import javelin.old.Screen;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.StatusPanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.overlay.BattleWalker;

/**
 * Screen context during battle.
 *
 * TODO it has become a hierarchy that behaves how different types of
 * {@link Fight}s should behave. The ideal would be for all this type of
 * controller behavior to move to {@link Fight}. For example: {@link LairScreen}
 * .
 *
 * TODO the 2.0 interface should absolutely not be redrawn every time, only when
 * an update is needed and even then the redraw should be on a tile-by-tile
 * basis, not the entire screen.
 *
 * TODO many things this is actually handling should be moved to {@link Fight}
 * controllers instead.
 *
 * TODO {@link BattleScreen} should not be the supertype for {@link WorldScreen}
 * and such. Extract a proper super type or move everything to a proper context
 * controller. Would probably be better to make this a unified ContextScreen and
 * delegate all differences to the context objects.
 *
 * @author alex
 */
public class BattleScreen extends Screen{
  static public Float lastaicheck=-Float.MAX_VALUE;
  /**
   * Active {@link BattleScreen} implementation.
   *
   * @see WorldScreen
   * @see DungeonScreen
   */
  public static BattleScreen active;
  /**
   * Keeps track of human {@link Movement} steps using the keyboard. We want to
   * allow them to move up to a {@link ActionCost#MOVE} action without
   * disruption their current action - even if {@link RepeatTurn} is thrown from
   * another action (like cancelling a menu, etc.).
   *
   * {@link BattleMouse} and {@link BattleWalker} take this into consideration
   * too, so as not to allow the player to cheat by using half their share of
   * keyboard movements and then using another {@link ActionCost#MOVE} worth of
   * movement witht the mouse. Mouse moves, however are instantaneous and apply
   * the relevant AP cost, plus the current value of this variable. This of
   * course still allows player to make shorter, tactical moves (consuming less
   * AP) if so desired.
   */
  public static float partialmove=0;

  static Runnable callback=null;

  /** Text output component. */
  public MessagePanel messagepanel;
  /** Unit info component. */
  public StatusPanel statuspanel;
  /** Visual representation of a {@link BattleState}. */
  public MapPanel mappanel;

  Combatant lastaimove;
  private boolean addsidebar;
  boolean maprevealed=false;
  Combatant current=null;
  boolean jointurns;

  /**
   * @param addsidebar If <code>true</code> will add a {@link StatusPanel} to
   *   this screen.
   */
  public BattleScreen(boolean addsidebar,boolean open){
    this.addsidebar=addsidebar;
    if(open) open();
  }

  void open(){
    BattleScreen.active=this;
    setForeground(Color.white);
    setBackground(Color.black);
    setLayout(new BorderLayout());
    messagepanel=new MessagePanel();
    add(messagepanel,"South");
    mappanel=getmappanel();
    mappanel.setup();
    add(mappanel,"Center");
    var p=new Panel();
    p.setLayout(new BorderLayout());
    add("East",p);
    statuspanel=new StatusPanel();
    if(addsidebar) p.add("Center",statuspanel);
    setFont(QuestApp.mainfont);
    Javelin.app.switchScreen(this);
    BattleScreen.active=this;
    Javelin.delayblock=false;
    partialmove=0;
    var l=getsquadlocation();
    mappanel.scroll.setSize(mappanel.getBounds().getSize());
    mappanel.zoom(0,l.x,l.y);
    mappanel.center(l.x,l.y,true);
    mappanel.scroll.setVisible(true);
  }

  /** @return Map panel implementation for this screen. */
  protected MapPanel getmappanel(){
    return new BattlePanel(Fight.state);
  }

  /** TODO on 2.0+, move to Context */
  public Point getsquadlocation(){
    return Fight.state.next.getlocation();
  }

  /**
   * this is the main game loop. it catches any exceptions for stability and
   * lets the game continue <br>
   * very important that endTurn() gets called after the player moves, this
   * ensures that the rest of the map stays up to date <br>
   */
  public void mainloop(){
    callback=null;
    while(true) turn();
  }

  /** Routine for human interaction. */
  protected void turn(){
    try{
      for(var c:Fight.state.getcombatants()) c.refresh();
      Fight.state.next();
      current=Fight.state.next;
      Examine.lastlooked=null;
      partialmove=0;
      checkai();
      if(Fight.state.redteam.contains(current)||current.automatic){
        lastaimove=current;
        computermove();
      }else{
        humanmove();
        lastaimove=null;
        jointurns=false;
      }
      updatescreen();
      block();
    }finally{
      var f=Fight.current;
      for(var m:f.mutators) m.endturn(f);
      f.checkend();
    }
  }

  synchronized void humanmove(){
    var s=Fight.state;
    lastaicheck=s.next.ap;
    if(current==null||current.automatic||s.fleeing.contains(current)) return;
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
    BattlePanel.current=current;
    center(current.location[0],current.location[1]);
    updatescreen();
    Interface.userinterface.waiting=true;
    final var updatableUserAction=callback==null?getUserInput():null;
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
    try{
      if(updatableUserAction==null) try{
        callback.run();
      }finally{
        callback=null;
      }
      else perform(convertEventToAction(updatableUserAction),
          updatableUserAction.isShiftDown());
    }catch(RepeatTurn e){
      MessagePanel.active.clear();
      humanmove();
    }
  }

  void computermove(){
    if(jointurns) jointurns=false;
    else{
      BattlePanel.current=current;
      MessagePanel.active.clear();
      if(MapPanel.overlay!=null) MapPanel.overlay.clear();
      Javelin.message("Thinking...\n",Javelin.Delay.NONE);
      messagepanel.repaint();
      updatescreen();
    }
    if(Javelin.DEBUG) Action.outcome(ThreadManager.think(Fight.state),true);
    else try{
      Action.outcome(ThreadManager.think(Fight.state),true);
    }catch(final RuntimeException e){
      Javelin.message("Fatal error: "+e.getMessage(),Javelin.Delay.NONE);
      messagepanel.repaint();
      throw e;
    }
  }

  /**
   * TODO this should eventually be replaced by a combatant-list UI that allows
   * you to uncheck {@link Combatant#automatic} at any point in time.
   */
  void checkai(){
    if(lastaicheck==null||lastaicheck+1>Fight.state.next.ap) return;
    lastaicheck=Fight.state.next.ap;
    for(Combatant c:Fight.state.blueteam)
      if(!c.automatic&&!c.source.passive) return;
    var prompt="""
        All of your units are in automatic mode. Continue?
        Press r to reset all your units to manual mode.
        Press n to not see this message again until the game is restarted.
        Press any other key to continue...""";
    var input=Javelin.prompt(prompt);
    messagepanel.clear();
    messagepanel.repaint();
    if(input=='n') lastaicheck=null;
    else if(input=='r') for(Combatant c:Fight.state.blueteam) c.automatic=false;
  }

  /**
   * Use this to break the input loop.
   *
   * @param r This will be run instead of an {@link Action} or
   *   {@link WorldAction}.
   * @see Mouse
   */
  static public void perform(Runnable r){
    callback=r;
    Interface.userinterface.go(null);
  }

  /** Processes {@link Javelin#delayblock}. */
  public void block(){
    if(Javelin.delayblock){
      Javelin.delayblock=false;
      Javelin.input();
      messagepanel.clear();
    }
  }

  /** TODO */
  public void view(int x,int y){
    var p=Fight.state.period;
    if(p.equals(Period.EVENING)||p.equals(Period.NIGHT))
      Fight.state.next.detect();
    else if(!maprevealed){
      for(javelin.view.mappanel.Tile[] ts:mappanel.tiles)
        for(javelin.view.mappanel.Tile t:ts) t.discovered=true;
      maprevealed=true;
    }
  }

  /** Like {@link MapPanel#center(int, int, boolean)} but without forcing. */
  public void center(int x,int y){
    mappanel.center(x,y,false);
  }

  boolean first=true;

  /** TODO black screen bug, hopefully this helps */
  public void fix(){
    //    if(!first) return;
    //    first=false;
    //    var s=BattleScreen.active.mappanel.getSize();
    //    var p=BattleScreen.active.mappanel.getPreferredSize();
    //    if(!s.equals(p)) BattleScreen.active.mappanel.setSize(p);
  }

  /** Redraws screen. */
  protected void updatescreen(){
    var current=Fight.state.clone(this.current);
    if(current!=null){
      var x=current.location[0];
      var y=current.location[1];
      center(x,y);
      view(x,y);
    }
    statuspanel.repaint();
    Javelin.redraw();
  }

  /**
   * @param state New state is {@link ChanceNode#n}.
   * @param enableoverrun If <code>true</code> may ignore {@link Delay#WAIT} and
   *   let the next automaric unit think instead.
   */
  public void setstate(final ChanceNode state,boolean enableoverrun){
    synchronized(MapPanel.PAINTER){
      if(MapPanel.overlay!=null) MapPanel.overlay.clear();
      MapPanel.overlay=state.overlay;
      BattlePanel.current=current;
      final var s=(BattleState)state.n;
      Fight.state=s;
      if(lastaimove==null) Javelin.redraw();
      var delay=state.delay;
      if(enableoverrun&&delay==Javelin.Delay.WAIT
          &&(s.redteam.contains(s.next)||s.next.automatic)){
        delay=Javelin.Delay.NONE;
        jointurns=true;
      }
      messagepanel.clear();
      statuspanel.repaint();
      Javelin.message(state.action,delay);
    }
  }

  /**
   * @return Gets action for this event
   * @throws RepeatTurn
   */
  public Action convertEventToAction(final KeyEvent keyEvent){
    if(rejectEvent(keyEvent)) throw new RepeatTurn();
    return ActionMapping.SINGLETON.getaction(keyEvent);
  }

  /**
   * @return User-input.
   */
  public KeyEvent getUserInput(){
    return Javelin.input();
  }

  /**
   * @param thing Visual representation of current unit.
   * @param action What is being performed.
   * @param isShiftDown Ignored.
   */
  void perform(final Action action,final boolean isShiftDown){
    try{
      current=Fight.state.clone(current);
      if(current.burrowed&&!action.allowburrowed) Dig.refuse();
      if(!action.perform(current)) throw new RepeatTurn();
    }catch(EndBattle e){
      throw e;
    }catch(Exception e){
      // TODO throw on debug?
      if(!(e instanceof RepeatTurn)) e.printStackTrace();
      throw new RepeatTurn();
    }
  }

  /**
   * TODO is needed?
   *
   * BUG Fix for
   * http://sourceforge.net/tracker/index.php?func=detail&aid=1088187
   * &group_id=16696&atid=116696 Ignore Alt keypresses, we may need to add more
   * of these for other platforms.
   */
  protected boolean rejectEvent(final KeyEvent keyEvent){
    return (keyEvent.getModifiers()|InputEvent.ALT_DOWN_MASK)!=0
        &&keyEvent.getKeyCode()==18;
  }

  /**
   * TODO with the {@link MapPanel} hierarchy now this is probably not needed
   * anymore
   */
  public Image gettile(int x,int y){
    var m=Fight.current.map;
    var s=m.map[x][y];
    if(s.blocked) return m.getblockedtile(x,y);
    return m.floor;
  }

  public void center(){
    if(current!=null) center(current.location[0],current.location[1]);
  }
}
