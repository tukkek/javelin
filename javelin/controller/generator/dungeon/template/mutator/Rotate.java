package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.generator.dungeon.template.MapTemplate;

public class Rotate extends Mutator{
	public static final Rotate INSTANCE=new Rotate();

	private Rotate(){
		chance=.5;
		allowcorridor=true;
	}

	@Override
	public void apply(MapTemplate t){
		char[][] rotated=new char[t.height][t.width];
		for(int x=0;x<t.width;x++)
			for(int y=0;y<t.height;y++)
				rotated[y][x]=t.tiles[x][y];
		t.tiles=rotated;
		t.width=rotated.length;
		t.height=rotated[0].length;
	}
}
