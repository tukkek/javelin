package javelin.controller.generator.dungeon.template.corridor;

import javelin.controller.Point;

public class SwervingCorridor extends WindingCorridor{
	public SwervingCorridor(){
		nearbyfloorlimit=Integer.MAX_VALUE;
	}

	@Override
	void generatesteps(){
		steps.add(new Point(+1,0));
		steps.add(new Point(-1,0));
		steps.add(new Point(0,+1));
		steps.add(new Point(0,-1));
	}
}
