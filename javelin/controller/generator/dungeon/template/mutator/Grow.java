package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.old.RPG;

public class Grow extends Mutator{
	public static final Grow INSTANCE=new Grow();

	private Grow(){
		chance=0.1;
	}

	@Override
	public void apply(MapTemplate t){
		if(t.width==1||t.height==1) return;
		int times=1;
		while(RPG.chancein(2))
			times+=1;
		for(int i=0;i<times;i++)
			grow(t,RPG.chancein(2),RPG.r(1,4));
	}

	public static void grow(MapTemplate t,boolean horizontal,int tiles){
		for(int i=0;i<tiles;i++)
			t.settiles(horizontal?growhorizontal(t):growvertical(t));
	}

	static char[][] growvertical(MapTemplate t){
		int slice=RPG.r(1,t.height-1);
		char[][] grown=new char[t.width][t.height+1];
		for(int x=0;x<t.width;x++){
			copyandslice(t.tiles[x],slice,grown[x],t.tiles[x].length);
			grown[x][slice]=grown[x][slice-1]==MapTemplate.WALL
					||grown[x][slice+1]==MapTemplate.WALL?MapTemplate.WALL:MapTemplate.FLOOR;
		}
		return grown;
	}

	static char[][] growhorizontal(MapTemplate t){
		int slice=RPG.r(1,t.width-1);
		char[][] grown=new char[t.width+1][t.height];
		copyandslice(t.tiles,slice,grown,t.tiles.length);
		for(int y=0;y<grown[0].length;y++)
			grown[slice][y]=grown[slice-1][y]==MapTemplate.WALL
					||grown[slice+1][y]==MapTemplate.WALL?MapTemplate.WALL:MapTemplate.FLOOR;
		return grown;
	}

	static void copyandslice(Object tiles,int slice,Object grown,int length){
		System.arraycopy(tiles,0,grown,0,slice);
		System.arraycopy(tiles,slice,grown,slice+1,length-slice);
	}
}
