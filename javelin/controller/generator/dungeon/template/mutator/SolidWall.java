package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;

public class SolidWall extends Wall{
	@Override
	protected Point[] getpossiblesteps(){
		return Point.getadjacentorthogonal();
	}

	@Override
	public boolean validatestep(Template t,Point step){
		return super.validatestep(t,step)&&t.countadjacent(Template.FLOOR,step)>1;
	}
}
