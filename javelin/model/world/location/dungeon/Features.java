package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Spirit;
import javelin.old.RPG;
import javelin.view.screen.DungeonScreen;

public class Features implements Iterable<Feature>,Serializable{
	/** Much faster, may help drawing {@link DungeonScreen}. */
	HashMap<Point,Feature> map=new HashMap<>();
	DungeonFloor dungeon;

	public Features(DungeonFloor d){
		dungeon=d;
	}

	@Override
	public Iterator<Feature> iterator(){
		return map.values().iterator();
	}

	public boolean isEmpty(){
		return map.isEmpty();
	}

	public void add(Feature f){
		if(Javelin.DEBUG&&f.getlocation()==null) throw new NullPointerException();
		map.put(f.getlocation(),f);
	}

	public ArrayList<Feature> copy(){
		return new ArrayList<>(map.values());
	}

	public void remove(Feature f){
		map.remove(f.getlocation());
	}

	public <K extends Feature> K get(Class<K> type){
		var all=getall(type);
		return all.isEmpty()?null:all.get(0);
	}

	public <K extends Feature> List<K> getall(Class<K> type){
		return map.values().stream().filter(f->type.isInstance(f)).map(f->(K)f)
				.collect(Collectors.toList());
	}

	public Feature get(int x,int y){
		return map.get(new Point(x,y));
	}

	public List<Feature> getallundiscovered(){
		return map.values().stream().filter(f->!dungeon.visible[f.x][f.y]||!f.draw)
				.collect(Collectors.toList());
	}

	/**
	 * TODO return a list so that {@link Spirit} can show the closest one. It'd be
	 * more versatile anyway.
	 */
	public Feature getundiscovered(){
		var undiscovered=getallundiscovered();
		return undiscovered.isEmpty()?null:RPG.pick(undiscovered);
	}

	public boolean has(Class<? extends Feature> feature){
		return dungeon.features.get(feature)!=null;
	}

	public void getknown(){
		int knowledge=Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE);
		int reveal=knowledge-(10+dungeon.level);
		while(dungeon.knownfeatures<reveal){
			dungeon.knownfeatures+=1;
			Feature f=getundiscovered();
			if(f!=null) dungeon.discover(f);
		}
	}

	/** @return A stream for functional processing. */
	public Stream<Feature> stream(){
		return map.values().stream();
	}

	public int size(){
		return map.size();
	}

	/** @return A copy of the internal Feature list. */
	public List<Feature> getall(){
		return new ArrayList<>(map.values());
	}
}
