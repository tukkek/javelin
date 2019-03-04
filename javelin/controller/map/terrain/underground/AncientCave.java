package javelin.controller.map.terrain.underground;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.old.RPG;
import javelin.old.underground.Caves;

/**
 * A big cave area with rock formations.
 *
 * @author alex
 */
public class AncientCave extends Caves{
	double borderratio=RPG.r(25,50)/100.0;
	int formationseeds=RPG.rolldice(4,8);
	double formationratio=RPG.r(10,25)/100.0;
	double debrisratio=RPG.r(0,50)/100.0;

	public AncientCave(){
		super("Ancient cave");
	}

	@Override
	public void generate(){
		var border=makeborder();
		var unnocupied=SIZE*SIZE-border.size();
		var formations=makeformations(Math.round(unnocupied*formationratio));
		placedebris(border,formations);
	}

	void placedebris(ArrayList<Point> border,ArrayList<Point> formations){
		var target=(SIZE*SIZE-border.size()-formations.size())*debrisratio;
		var debris=0;
		while(debris<target){
			var p=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
			var tile=map[p.x][p.y];
			if(tile.blocked||tile.obstructed) continue;
			tile.obstructed=true;
			debris+=1;
		}
	}

	ArrayList<Point> makeformations(Long target){
		var formations=new ArrayList<Point>(target.intValue());
		while(formations.size()<formationseeds){
			var p=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
			if(!map[p.x][p.y].blocked){
				map[p.x][p.y].blocked=true;
				formations.add(p);
			}
		}
		while(formations.size()<target)
			expand(formations);
		return formations;
	}

	ArrayList<Point> makeborder(){
		var border=new ArrayList<>(close());
		var target=Math.round(borderratio*SIZE*SIZE);
		while(border.size()<target)
			expand(border);
		return border;
	}

	void expand(ArrayList<Point> border){
		var p=new Point(RPG.pick(border));
		while(map[p.x][p.y].blocked){
			p.displaceaxis();
			if(!p.validate(0,0,SIZE,SIZE)) return;
		}
		map[p.x][p.y].blocked=true;
		border.add(p);
	}
}
