package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Spirit;
import javelin.old.RPG;

public class Features implements Iterable<Feature>,Serializable{
	List<Feature> list=new ArrayList<>();
	Dungeon dungeon;

	public Features(Dungeon d){
		super();
		dungeon=d;
	}

	@Override
	public Iterator<Feature> iterator(){
		return list.iterator();
	}

	public boolean isEmpty(){
		return list.isEmpty();
	}

	public void add(Feature f){
		list.add(f);
	}

	public ArrayList<Feature> copy(){
		return new ArrayList<>(list);
	}

	public void remove(Feature f){
		list.remove(f);
	}

	public <K extends Feature> K get(Class<K> type){
		var all=getall(type);
		return all.isEmpty()?null:all.get(0);
	}

	public <K extends Feature> List<K> getall(Class<K> type){
		return list.stream().filter(f->type.isInstance(f)).map(f->(K)f)
				.collect(Collectors.toList());
	}

	public Feature get(int x,int y){
		for(Feature f:this)
			if(f.x==x&&f.y==y) return f;
		return null;
	}

	public List<Feature> getallundiscovered(){
		return list.stream().filter(f->!dungeon.visible[f.x][f.y]||!f.draw)
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
		while(dungeon.revealed<reveal){
			dungeon.revealed+=1;
			Feature f=getundiscovered();
			if(f!=null) dungeon.discover(f);
		}
	}

	/** @return A stream for functional processing. */
	public Stream<Feature> stream(){
		return list.stream();
	}

	public int size(){
		return list.size();
	}
}
