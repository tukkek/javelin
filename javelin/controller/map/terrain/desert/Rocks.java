package javelin.controller.map.terrain.desert;

import java.util.List;

import javelin.controller.Point;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

public class Rocks extends Sandy{
	public Rocks(){
		name="Desert rocks";
		wall=Images.get(List.of("terrain","ruggedwall"));
	}

	@Override
	public void generate(){
		super.generate();
		int rocks=0;
		for(int i=0;i<5;i++)
			rocks+=RPG.r(1,4);
		for(int i=0;i<rocks;i++)
			generaterock();
	}

	void generaterock(){
		int width=RPG.r(1,4)+1;
		int height=RPG.r(1,4)+RPG.r(1,4);
		if(RPG.chancein(2)){
			int swap=width;
			width=height;
			height=swap;
		}
		Point p=new Point(RPG.r(0,map.length),RPG.r(0,map[0].length));
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++){
				Point rock=new Point(p.x+x,p.y+y);
				if(rock.validate(0,0,map.length,map[0].length)){
					Square s=map[rock.x][rock.y];
					s.clear();
					s.blocked=true;
				}
			}
	}
}
