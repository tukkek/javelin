package javelin.controller;

import java.util.HashSet;

import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;

/**
 * Uses a flooding alghoritm to traverse a portion of a {@link Dungeon}.
 *
 * @author alex
 */
public class DungeonMapCrawler{
	HashSet<Point> visited=new HashSet<>();
	Point origin;
	int depth;

	protected Dungeon dungeon;

	/**
	 * @param origin Starting point (included in result set).
	 * @param depth How many steps at most to take.
	 * @param d Dungeon instance being traversed.
	 */
	public DungeonMapCrawler(Point origin,int depth,Dungeon d){
		this.origin=origin;
		this.depth=depth;
		dungeon=d;
	}

	/**
	 * @return All dungeon tiles traversed.
	 */
	public HashSet<Point> crawl(){
		crawl(origin,0);
		return visited;
	}

	/**
	 * @return <code>true</code> for a valid Point;
	 */
	protected boolean validate(Point p){
		return p.validate(0,0,dungeon.map.length,dungeon.map[0].length);
	}

	/**
	 * Executed after {@link #validate(Point)}.
	 *
	 * @param f May be <code>null</code>;
	 * @return <code>true</code> for a valid Feature in a given Point.
	 */
	protected boolean validate(Feature f){
		return true;
	}

	/**
	 * Only called for valid Points and Features.
	 *
	 * @param f May be <code>null</code>;
	 * @return <code>true</code> to continue traversing from this point onwards.
	 */
	public boolean proceed(Point p,Feature f){
		return dungeon.map[p.x][p.y]!=MapTemplate.WALL&&!(f instanceof Door);
	}

	void crawl(Point p,int depth){
		if(depth>this.depth||visited.contains(p)||!validate(p)) return;
		Feature f=dungeon.features.get(p.x,p.y);
		if(!validate(f)) return;
		visited.add(p);
		if(proceed(p,f)) for(Point adjacent:p.getadjacent())
			crawl(adjacent,depth+1);
	}
}