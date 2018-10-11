package javelin.controller.generator.dungeon.template.generated;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.Direction;
import javelin.old.RPG;

public class Geometric extends Linear{
	public Geometric(){
		minsize=8;
	}

	@Override
	List<Point> getdots(){
		int sides=RPG.r(3,Math.max(width,height));
		float dotsperborder=sides/4f;
		ArrayList<Point> dots=new ArrayList<>();
		float dotpool=0;
		for(int i=RPG.r(0,3);sides>0;i++){
			i=i%4;
			dotpool+=dotsperborder;
			int ndots=Math.round(Math.round(Math.floor(dotpool)));
			dotpool-=ndots;
			placedots(ndots,Direction.ALL[i],dots);
			sides-=ndots;
		}
		return dots;
	}

	void placedots(int ndots,Direction d,ArrayList<Point> dots){
		if(ndots==0) return;
		int axis=d==Direction.EAST||d==Direction.WEST?height:width;
		float step=axis/(ndots+1f);
		for(int i=1;i<=ndots;i++){
			int position=Math.round(step*i);
			Point p=new Point(position,position);
			if(d==Direction.NORTH)
				p.y=height-1;
			else if(d==Direction.SOUTH)
				p.y=0;
			else if(d==Direction.WEST)
				p.x=0;
			else
				p.x=width-1;
			Point bump=null;
			while(bump==null||!bump.validate(0,0,width,height)){
				bump=p.clone();
				bump.x+=RPG.r(-1,+1);
				bump.y+=RPG.r(-1,+1);
			}
			dots.add(p);
		}
	}
}
