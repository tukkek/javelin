package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

/** @see BreathWeapon */
public class BreathOverlay extends Overlay{
  Border border;

  /** Constructor. */
  public BreathOverlay(Set<Point> area){
    affected.addAll(area);
    var size=MapPanel.get().gettilesize();
    border=BorderFactory.createLineBorder(Color.CYAN,size/10);
  }

  @Override
  public void overlay(Tile t,Graphics g){
    if(!affected.contains(new Point(t.x,t.y))) return;
    var map=MapPanel.get();
    var s=map.gettilesize();
    var p=t.getposition();
    border.paintBorder(map.canvas,g,p.x,p.y,s,s);
  }
}
