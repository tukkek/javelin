package javelin.controller.comparator;

import java.io.Serializable;
import java.util.Comparator;

import javelin.model.unit.Monster;

public class MonstersByCr implements Comparator<Monster>,Serializable{
	public static final MonstersByCr SINGLETON=new MonstersByCr();

	private MonstersByCr(){
		// prevent instantiation
	}

	@Override
	public int compare(Monster a,Monster b){
		var delta=Double.compare(a.cr,b.cr);
		return delta==0?a.name.compareTo(b.name):delta;
	}
}
