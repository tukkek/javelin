package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javelin.model.world.location.dungeon.feature.Feature;

public class Features implements Iterable<Feature>,Serializable{
	List<Feature> features=new ArrayList<>();
	Dungeon dungeon;

	public Features(Dungeon d){
		super();
		dungeon=d;
	}

	@Override
	public Iterator<Feature> iterator(){
		return features.iterator();
	}

	public boolean isEmpty(){
		return features.isEmpty();
	}

	public void add(Feature f){
		features.add(f);
		f.place(dungeon);
	}

	public ArrayList<Feature> copy(){
		return new ArrayList<>(features);
	}

	public void remove(Feature f){
		features.remove(f);
	}

}
