package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.old.RPG;

public class Symmetry extends Mutator{
	public Symmetry(){
		chance=0.1;
	}

	@Override
	public void apply(MapTemplate t){
		boolean horizontal=false;
		boolean vertical=false;
		while(!horizontal&&!vertical){
			horizontal=RPG.chancein(2);
			vertical=RPG.chancein(2);
		}
		if(horizontal&&vertical){
			if(RPG.chancein(2)){
				apply(t,true);
				apply(t,false);
			}else{
				apply(t,false);
				apply(t,true);
			}
		}else if(horizontal)
			apply(t,true);
		else if(vertical) apply(t,false);
	}

	void apply(MapTemplate t,boolean horizontal){
		int axis=(horizontal?t.width:t.height)-1;
		boolean reverse=RPG.chancein(2);
		for(int cursor=0;cursor<axis-cursor+1;cursor++){
			int from=cursor;
			int to=axis-cursor;
			if(reverse){
				int swap=from;
				from=to;
				to=swap;
			}
			if(horizontal)
				copyhorizontal(t,from,to);
			else
				copyvertical(t,from,to);
		}
	}

	void copyvertical(MapTemplate t,int from,int to){
		for(int x=0;x<t.width;x++)
			t.tiles[x][to]=t.tiles[x][from];
	}

	void copyhorizontal(MapTemplate t,int from,int to){
		for(int y=0;y<t.height;y++)
			t.tiles[to][y]=t.tiles[from][y];
	}
}
