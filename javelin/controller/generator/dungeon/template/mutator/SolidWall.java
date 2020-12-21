package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;

public class SolidWall extends Wall{
	@Override
	protected Point[] getpossiblesteps(){
		return Point.getadjacentorthogonal();
	}

	@Override
	public boolean validatestep(FloorTile t,Point step){
		return super.validatestep(t,step)&&t.countadjacent(FloorTile.FLOOR,step)>1;
	}
}
