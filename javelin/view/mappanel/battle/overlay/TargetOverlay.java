package javelin.view.mappanel.battle.overlay;

import java.awt.Image;
import java.util.List;

import javelin.controller.Point;
import javelin.view.Images;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

public class TargetOverlay extends Overlay{
  public static final Image TARGET=Images.get(List.of("overlay","target"));

  public int x;
  public int y;

  Tile t=null;

  public TargetOverlay(int x,int y){
    this.x=x;
    this.y=y;
    affected.add(new Point(x,y));
    BattleScreen.active.mappanel.tiles[x][y].repaint();
  }

  public TargetOverlay(Point p){
    this(p.x,p.y);
  }

  @Override
  public void overlay(Tile t){
    if(t.x==x&&t.y==y){
      draw(t,TARGET);
      BattleScreen.active.center(x,y);
    }
  }
}
