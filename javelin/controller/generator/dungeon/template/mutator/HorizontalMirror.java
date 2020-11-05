package javelin.controller.generator.dungeon.template.mutator;

import java.util.Arrays;

import javelin.controller.generator.dungeon.template.MapTemplate;

public class HorizontalMirror extends Mutator{
	public static final HorizontalMirror INSTANCE=new HorizontalMirror();

	private HorizontalMirror(){
		chance=.5;
		allowcorridor=true;
	}

	@Override
	public void apply(MapTemplate t){
		char[][] original=Arrays.copyOf(t.tiles,t.width);
		for(int x=0;x<t.width;x++)
			t.tiles[t.width-x-1]=original[x];
	}
}
