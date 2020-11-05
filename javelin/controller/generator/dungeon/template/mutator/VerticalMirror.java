package javelin.controller.generator.dungeon.template.mutator;

import java.util.Arrays;

import javelin.controller.generator.dungeon.template.MapTemplate;

public class VerticalMirror extends Mutator{
	public static final VerticalMirror INSTANCE=new VerticalMirror();

	private VerticalMirror(){
		chance=.5;
		allowcorridor=true;
	}

	@Override
	public void apply(MapTemplate t){
		for(int x=0;x<t.width;x++){
			char[] original=Arrays.copyOf(t.tiles[x],t.height);
			for(int y=0;y<t.height;y++)
				t.tiles[x][t.height-1-y]=original[y];
		}
	}
}
