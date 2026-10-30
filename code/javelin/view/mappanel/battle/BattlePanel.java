package javelin.view.mappanel.battle;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.content.fight.Fight;
import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Period;
import javelin.view.OpenGL;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

/** @author alex */
public class BattlePanel extends MapPanel{
  /// Active unit. TODO is it redundant with {@link BattleState#next}?
  static public Combatant current=null;

  /** If <code>true</code>, all {@link BattleTile}s are visible. */
  public boolean daylight;

  Set<BattleTile> seen=new HashSet<>();
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
    var point=current.getlocation();
    grid.draw(tiles[point.x][point.y],g);
  }

  public void calculatevision(){
    synchronized(OpenGL.LOCK){
      var p=state.period;
      daylight=p.equals(Period.MORNING)||p.equals(Period.AFTERNOON);
      if(previous==null||p!=previous.period)
        for(var tiles:this.tiles) for(var tile:tiles){
          var bt=(BattleTile)tile;
          bt.shrouded=!daylight;
          if(daylight) seen.add(bt);
        }
      if(daylight) return;
      for(var tile:seen) tile.shrouded=true;
      for(var tile:current.calculatevision(state)){
        var bt=(BattleTile)tiles[tile.x][tile.y];
        seen.add(bt);
        bt.discovered=true;
        bt.shrouded=false;
      }
    }
  }

  public void updatestate(){
    previous=state;
    state=Fight.state.clonedeeply();
    current=state.clone(current);
    BattleTile.panel=this;
    calculatevision();
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
