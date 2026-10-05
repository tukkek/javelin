package javelin.controller.content.map.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;

/**
 * A map read and interpreted from a text file on the "maps" folder.
 *
 * @author alex
 */
public class LocationMap extends Map{
  /**
   * Possible starting positions for the player team. If empty, will be ignored.
   */
  public ArrayList<Point> spawnblue=new ArrayList<>(0);
  /** Same as {@link #spawnblue} but for enemies. */
  public ArrayList<Point> spawnred=new ArrayList<>(0);

  /** Constructor. */
  public LocationMap(String name){
    super(name,0,0);
  }

  /**
   * @param filename Will read this from inside the maps folder.
   * @return Each character represents a tile, each line a {@link Map} row.
   */
  public static List<String> read(String filename){
    var f=new File("maps",filename+".txt");
    try(var r=new BufferedReader(new FileReader(f))){
      var map=new ArrayList<String>(DndMap.SIZE);
      for(var l=r.readLine();l!=null;l=r.readLine()) map.add(l);
      return map;
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generate(){
    var filename=name.replaceAll(" ","").toLowerCase();
    var map=read(filename);
    var height=map.get(0).length();
    var width=map.size();
    if(height!=width) throw new RuntimeException(
        "Maps need to be square (same width and height).");
    this.map=new Square[height][width];
    for(var x=0;x<width;x++){
      var line=map.get(x).toCharArray();
      for(var y=0;y<line.length;y++){
        var s=new Square();
        this.map[y][x]=s;
        processtile(s,y,x,line[y]);
      }
    }
  }

  protected Square processtile(Square s,int x,int y,char c){
    if(c=='~') s.flooded=true;
    else if(c=='#') s.blocked=true;
    else if(c=='x') s.obstructed=true;
    else if(c=='1') spawnblue.add(new Point(x,y));
    else if(c=='2') spawnred.add(new Point(x,y));
    return s;
  }

  @Override
  public List<Point> getspawn(List<Combatant> team){
    return team==Fight.state.redteam?spawnred:spawnblue;
  }
}
