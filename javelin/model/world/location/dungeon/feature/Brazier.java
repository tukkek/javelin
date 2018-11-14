package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.DungeonCrawler;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.FireTemple;

/**
 * @see FireTemple
 * @author alex
 */
public class Brazier extends Feature{
	static final int RADIUS=13;

	/** Constructor. */
	public Brazier(){
		super("dungeonbrazier");
	}

	@Override
	public boolean activate(){
		DungeonCrawler crawler=new DungeonCrawler(new Point(x,y),RADIUS,
				Dungeon.active);
		for(Point p:crawler.crawl())
			brighten(p);
		Point p=JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x,p.y);
		Javelin.message("You light up the brazier!",false);
		return true;
	}

	void brighten(Point p){
		Dungeon.active.setvisible(p.x,p.y);
		Feature f=Dungeon.active.features.get(p.x, p.y);
		if(f!=null) f.discover(null,9000);
	}
}
