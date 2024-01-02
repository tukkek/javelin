package javelin.view.mappanel.battle;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Meld;
import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Period;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

/** @author alex */
public class BattlePanel extends MapPanel{
  /** Active unit. */
  static public Combatant current=null;

  /** If <code>true</code>, all {@link BattleTile}s are visible. */
  public boolean daylight;

  Set<Point> seen=new HashSet<>();
  BattleState previous=null;
  BattleState state=null;

  /** Constructor. */
  public BattlePanel(BattleState s){
    super(s.map.length,s.map[0].length,Preferences.KEYTILEBATTLE);
    var p=Fight.current.period;
    daylight=p.equals(Period.MORNING)||p.equals(Period.AFTERNOON);
  }

  @Override
  protected Mouse getmouselistener(){
    return new BattleMouse(this);
  }

  @Override
  protected Tile newtile(int x,int y){
    return new BattleTile(x,y,daylight);
  }

  @Override
  public void refresh(){
    updatestate();
    var s=Fight.state;
    var update=new HashSet<Point>(s.redteam.size()+s.blueteam.size());
    for(var c:s.getcombatants()) update.add(c.getlocation());
    if(previous!=null)
      for(var c:previous.getcombatants()) update.add(c.getlocation());
    updatestate();
    for(var c:s.getcombatants()) update.add(c.getlocation());
    calculatevision();
    synchronized(seen){
      update.addAll(seen);
    }
    if(overlay!=null) update.addAll(overlay.affected);
    if(Fight.current.has(Meld.class)!=null)
      for(var m:s.meld) update.add(new Point(m.x,m.y));
    for(var p:update) tiles[p.x][p.y].repaint();
  }

  void calculatevision(){
    if(daylight||state.getteam(current)==state.redteam) return;
    synchronized(seen){
      for(var s:seen){
        var t=(BattleTile)tiles[s.x][s.y];
        t.shrouded=true;
      }
      var vision=current.calculatevision(state);
      for(var v:vision){ // seen
        var t=(BattleTile)tiles[v.x][v.y];
        seen.add(v);
        t.discovered=true;
        t.shrouded=false;
      }
    }
  }

  void updatestate(){
    previous=state;
    state=Fight.state.clonedeeply();
    current=state.clone(current);
    BattleTile.panel=this;
    var p=state.period;
    daylight=p.equals(Period.MORNING)||p.equals(Period.AFTERNOON);
    if(previous==null||p!=previous.period) synchronized(seen){
      for(var tiles:tiles) for(var t:tiles){
        var bt=(BattleTile)t;
        bt.shrouded=!daylight;
        if(daylight) seen.add(new Point(t.x,t.y));
      }
    }
  }

  @Override
  protected int gettilesize(){
    return Preferences.tilesizebattle;
  }
}
