package javelin.view.mappanel.battle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.content.fight.Fight;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class BattleTile extends Tile{
  public static final float MAXLIFE=Combatant.STATUSUNHARMED;
  static final Border BUFF=BorderFactory.createLineBorder(Color.WHITE);

  public static MapPanel panel=null;
  private Image obstacle;
  public boolean shrouded;

  public BattleTile(final int xp,final int yp,final boolean discoveredp){
    super(xp,yp,discoveredp);
    shrouded=!discovered;
  }

  @Override
  public void paint(final Graphics g){
    if(g==null) return;
    if(!discovered){
      drawcover(g);
      return;
    }
    final var m=Fight.current.map;
    final var s=Fight.state.map[x][y];
    if(!s.blocked){
      draw(g,m.getfloor(x,y));
      if(s.obstructed){
        if(obstacle==null) obstacle=m.getobstacle(x,y);
        draw(g,obstacle);
      }
    }else{
      if(m.wallfloor!=null) draw(g,m.wallfloor);
      else draw(g,m.getfloor(x,y));
      draw(g,m.wall);
    }
    if(s.flooded) draw(g,m.flooded);
    final var c=Fight.state.getcombatant(x,y);
    if(c!=null) drawcombatant(g,c,this);
    else for(MeldCrystal meld:Fight.state.meld)
      if(meld.x==x&&meld.y==y) draw(g,meld.getimage(Fight.state));
    if(shrouded){
      var p=getposition();
      g.setColor(new Color(0,0,0,1/3f));
      g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
    }
    if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
  }

  void drawcombatant(final Graphics g,final Combatant c,final Tile t){
    final var isblueteam=Fight.state.blueteam.contains(c);
    var p=getposition();
    if(BattlePanel.current.equals(c)){
      g.setColor(isblueteam?Color.GREEN:Color.ORANGE);
      g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
    }
    draw(g,Images.get(c));
    g.setColor(isblueteam?Color.BLUE:Color.RED);
    final var hp=MapPanel.tilesize-MapPanel.tilesize*c.hp/c.getmaxhp();
    g.fillRect(p.x,p.y+hp,MapPanel.tilesize/10,MapPanel.tilesize-hp);
    if(c.ispenalized(Fight.state)){
      final var penalized=Images.PENALIZED.getScaledInstance(MapPanel.tilesize,
          MapPanel.tilesize,Image.SCALE_DEFAULT);
      g.drawImage(penalized,p.x,p.y,null);
    }
    if(c.isbuffed()) BUFF.paintBorder(panel.canvas,g,p.x,p.y,MapPanel.tilesize,
        MapPanel.tilesize);
    if(c.source.elite){
      final var elite=Images.ELITE.getScaledInstance(MapPanel.tilesize,
          MapPanel.tilesize,Image.SCALE_DEFAULT);
      g.drawImage(elite,p.x,p.y,null);
    }
    if(c.mercenary){
      final var mercenary=Images.MERCENARY.getScaledInstance(MapPanel.tilesize,
          MapPanel.tilesize,Image.SCALE_DEFAULT);
      g.drawImage(mercenary,p.x,p.y,null);
    }else if(c.summoned){
      final var summoned=Images.SUMMONED.getScaledInstance(MapPanel.tilesize,
          MapPanel.tilesize,Image.SCALE_DEFAULT);
      g.drawImage(summoned,p.x,p.y,null);
    }
  }
}
