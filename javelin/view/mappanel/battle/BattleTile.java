package javelin.view.mappanel.battle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class BattleTile extends Tile{
  public static float MAXLIFE=Combatant.STATUSUNHARMED;
  public static MapPanel panel=null;

  static Border BUFF=BorderFactory.createLineBorder(Color.WHITE);

  public boolean shrouded;

  Image obstacle;

  public BattleTile(int x,int p,boolean discoveredp){
    super(x,p,discoveredp);
    shrouded=!discovered;
  }

  @Override
  public void paint(Graphics g){
    //    if(g==null) return;
    if(!discovered){
      drawcover(g);
      return;
    }
    var m=Fight.current.map;
    var s=Fight.state;
    var square=s.map[x][y];
    if(square.blocked){
      if(m.wallfloor!=null) draw(g,m.wallfloor);
      else draw(g,m.getfloor(x,y));
      draw(g,m.wall);
    }else{
      draw(g,m.getfloor(x,y));
      if(square.obstructed){
        if(obstacle==null) obstacle=m.getobstacle(x,y);
        draw(g,obstacle);
      }
    }
    if(square.flooded) draw(g,m.flooded);
    var c=s.getcombatant(x,y);
    var meld=s.getmeld(x,y);
    if(c!=null) drawcombatant(g,c,this);
    else if(meld!=null) draw(g,meld.getimage(s));
    if(shrouded){
      var p=getposition();
      g.setColor(new Color(0,0,0,1/3f));
      g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
    }
    if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
  }

  void draw(Image i,Point p,Graphics g){
    var size=MapPanel.tilesize;
    var s=i.getScaledInstance(size,size,Image.SCALE_DEFAULT);
    g.drawImage(s,p.x,p.y,null);
  }

  void drawcombatant(Graphics g,Combatant c,Tile t){
    var isblue=Fight.state.blueteam.contains(c);
    var s=MapPanel.tilesize;
    var p=getposition();
    if(c.equals(BattlePanel.current)){
      g.setColor(isblue?Color.GREEN:Color.ORANGE);
      g.fillRect(p.x,p.y,s,s);
    }
    draw(g,Images.get(c));
    g.setColor(isblue?Color.BLUE:Color.RED);
    var hp=s-s*c.hp/c.getmaxhp();
    g.fillRect(p.x,p.y+hp,s/10,s-hp);
    if(c.ispenalized(Fight.state)) draw(Images.PENALIZED,p,g);
    if(c.isbuffed()) BUFF.paintBorder(panel.canvas,g,p.x,p.y,s,s);
    if(c.source.elite) draw(Images.ELITE,p,g);
    if(c.mercenary) draw(Images.MERCENARY,p,g);
    else if(c.summoned) draw(Images.SUMMONED,p,g);
  }
}
