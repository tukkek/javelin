package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javelin.Javelin;

public class Combatants extends ArrayList<Combatant>
		implements Cloneable,Serializable{

	public Combatants(){
		super();
	}

	public Combatants(int size){
		super(size);
	}

	public Combatants(Collection<Combatant> list){
		super(list);
	}

	@Override
	public int hashCode(){
		return toString().hashCode();
	}

	@Override
	public String toString(){
		return Javelin.group(this);
	}

	@Override
	public boolean equals(Object o){
		return o instanceof Combatants&&hashCode()==o.hashCode();
	}

	@Override
	public Combatants clone(){
		Combatants clone=(Combatants)super.clone();
		for(int i=0;i<size();i++)
			clone.set(i,get(i).clone());
		return clone;
	}

	public List<Monster> getmonsters(){
		ArrayList<Monster> monsters=new ArrayList<>(size());
		for(Combatant c:this)
			monsters.add(c.source);
		return monsters;
	}

	/**
	 * Unlike {@link #clone()}, this returns a new group of {@link Combatant}s,
	 * with new {@link Combatant#id}s.
	 */
	public Combatants generate(){
		var encounter=new Combatants(size());
		for(final Combatant m:this)
			encounter.add(new Combatant(m.source,true));
		return encounter;
	}

	/** @return Lowest {@link Monster#cr}. */
	public Combatant getweakest(){
		return stream().sorted((a,b)->Float.compare(a.source.cr,b.source.cr))
				.findFirst().orElse(null);
	}
}
