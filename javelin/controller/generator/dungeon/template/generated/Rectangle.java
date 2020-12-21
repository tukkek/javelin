package javelin.controller.generator.dungeon.template.generated;

import javelin.controller.generator.dungeon.template.FloorTile;

public class Rectangle extends FloorTile{
	public Rectangle(){
		mutate=1;
	}

	@Override
	public void generate(){
		width=0;
		while(width<2||height<2)
			initrandom();
	}
}
