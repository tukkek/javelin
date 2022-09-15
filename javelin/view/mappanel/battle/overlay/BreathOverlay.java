package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

/** @see BreathWeapon */
public class BreathOverlay extends Overlay{
  private static final Border BORDER=BorderFactory.createLineBorder(Color.CYAN,
      MapPanel.tilesize/10);

  /** Constructor. */
  public BreathOverlay(Set<Point> area){
    affected.addAll(area);
  }

  @Override
  public void overlay(Tile t){
    if(!affected.contains(new Point(t.x,t.y))) return;
    var map=BattleScreen.active.mappanel;
    var g=map.getdrawgraphics();
    var s=MapPanel.tilesize;
    var p=t.getposition();
    BORDER.paintBorder(map.canvas,g,p.x,p.y,s,s);
  }
}
