package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.MapTemplate;

public class SolidWall extends Wall{
	@Override
	protected Point[] getpossiblesteps(){
		return Point.getadjacentorthogonal();
	}

	@Override
	public boolean validatestep(MapTemplate t,Point step){
		return super.validatestep(t,step)&&t.countadjacent(MapTemplate.FLOOR,step)>1;
	}
}
