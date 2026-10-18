package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;

import javelin.model.world.location.town.Town;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.MapPanel.Grid;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

public class DistrictOverlay extends Overlay{
  static final javax.swing.border.Border WHITEBORDER=BorderFactory
      .createLineBorder(Color.WHITE,1);

  public DistrictOverlay(Town target){
    var tiles=target.getdistrict().getarea().stream()
        .map(point->MapPanel.get().tiles[point.x][point.y]).toList();
    affected.addAll(tiles);
  }

  @Override
  public void overlay(Tile tile,Graphics g){
    var point=Grid.get().position(tile);
    if(affected.contains(tile)) paint(point.x,point.y,g);
  }

  static public void paint(int canvasx,int canvasy,Graphics g){
    var map=MapPanel.get();
    var size=map.gettilesize();
    WHITEBORDER.paintBorder(map.canvas,g,canvasx,canvasy,size,size);
  }
}
