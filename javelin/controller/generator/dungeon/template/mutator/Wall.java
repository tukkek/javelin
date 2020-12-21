package javelin.controller.generator.dungeon.template.mutator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.old.RPG;

public class Wall extends Mutator{
	@Override
	public void apply(FloorTile t){
		for(int i=0;i<RPG.r(1,4);i++)
			if(!generatewall(t)) return;
	}

	boolean generatewall(FloorTile t){
		LinkedList<Point> spaces=new LinkedList<>(t.find(FloorTile.FLOOR));
		Collections.shuffle(spaces);
		while(!spaces.isEmpty()){
			Point p=spaces.pop();
			if(t.countadjacent(FloorTile.FLOOR,p)>4){
				carve(t,p);
				return true;
			}
		}
		return false;
	}

	void carve(FloorTile t,Point p){
		for(int length=RPG.r(1,t.width+t.height);length>0;length--){
			t.tiles[p.x][p.y]=FloorTile.WALL;
			ArrayList<Point> next=getsteps(t,p);
			if(next.isEmpty()) return;
			p=RPG.pick(next);
		}
	}

	ArrayList<Point> getsteps(FloorTile t,Point p){
		ArrayList<Point> next=new ArrayList<>(8);
		for(Point step:getpossiblesteps()){
			step.x+=p.x;
			step.y+=p.y;
			if(validatestep(t,step)) next.add(step);
		}
		return next;
	}

	public boolean validatestep(FloorTile t,Point step){
		return step.validate(0,0,t.width,t.height)
				&&t.tiles[step.x][step.y]==FloorTile.FLOOR;
	}

	protected Point[] getpossiblesteps(){
		return Point.getadjacent2();
	}
}
