package javelin.view.mappanel.battle;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Period;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

/** @author alex */
public class BattlePanel extends MapPanel{
  /// Active unit. TODO is it redundant with {@link BattleState#next}?
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
    return new BattleMouse();
  }

  @Override
  protected Tile newtile(int x,int y){
    return new BattleTile(x,y,daylight);
  }

  @Override
  public void refresh(Graphics g){
    var state=Fight.state;
    if(state==null) return;
    updatestate();
    calculatevision();
    var point=current.getlocation();
    grid.draw(tiles[point.x][point.y],g);
  }

  void calculatevision(){
    if(daylight) return;
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

  void updatestate(){
    previous=state;
    state=Fight.state.clonedeeply();
    current=state.clone(current);
    BattleTile.panel=this;
    var p=state.period;
    daylight=p.equals(Period.MORNING)||p.equals(Period.AFTERNOON);
    if(previous==null||p!=previous.period)
      for(var tiles:tiles) for(var t:tiles){
        var bt=(BattleTile)t;
        bt.shrouded=!daylight;
        if(daylight) seen.add(new Point(t.x,t.y));
      }
  }

  @Override
  public int gettilesize(){
    return Preferences.tilesizebattle;
  }

  @Override
  protected void settilesize(int size){
    Preferences.tilesizebattle=size;
  }
}
