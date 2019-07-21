package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.Monster;

public class MonstersByCr implements Comparator<Monster>{
	public static final MonstersByCr SINGLETON=new MonstersByCr();

	@Override
	public int compare(Monster a,Monster b){
		var delta=Double.compare(a.cr,b.cr);
		return delta==0?a.name.compareTo(b.name):delta;
	}
}
