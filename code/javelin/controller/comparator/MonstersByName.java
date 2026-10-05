package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.Monster;

public class MonstersByName implements Comparator<Monster>{
	public static final Comparator<? super Monster> INSTANCE=new MonstersByName();

	private MonstersByName(){
		// prevent instantiation
	}

	@Override
	public int compare(Monster o1,Monster o2){
		return o1.name.compareTo(o2.name);
	}
}
