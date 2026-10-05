package javelin.controller.generator.dungeon.template.corridor;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.old.RPG;

public class WindingCorridor extends StraightCorridor{
  protected ArrayList<Point> steps;
  protected int nearbyfloorlimit=1;

  public WindingCorridor(){
    fill=WALL;
    steps=new ArrayList<>();
    generatesteps();
    steps.remove(new Point(0,0));
  }

  void generatesteps(){
    for(var x=-1;x<=+1;x++) for(var y=-1;y<=+1;y++) steps.add(new Point(x,y));
  }

  @Override
  public void generate(DungeonGenerator g){
    width=0;
    while(width<3||height<3) initrandom(g);
    var d=Direction.getrandom();
    var p=RPG.pick(d.getborder(this));
    var steps=new HashSet<Point>();
    while(p.validate(0,0,width,height)){
      tiles[p.x][p.y]=FLOOR;
      var step=RPG.pick(this.steps);
      if(step.x==d.following.x||step.y==d.following.y) continue;
      step=new Point(p.x+step.x,p.y+step.y);
      if(steps.contains(step)||countadjacent(FLOOR,step)>nearbyfloorlimit||(steps.size()==0&&!step.validate(0,0,width,height))) continue;
      steps.add(step);
      p=step;
    }
  }

  @Override
  protected boolean validate() throws GaveUp{
    return super.validate()&&count(DOOR)>1;
  }
}
