package javelin.controller.table;

import java.io.Serializable;
import java.util.HashMap;

import javelin.model.world.location.dungeon.DungeonFloor;

public class Tables implements Serializable,Cloneable{
	HashMap<Class<? extends Table>,Table> tables=new HashMap<>();

	public <K extends Table> K get(Class<K> table,DungeonFloor f){
		try{
			var t=tables.get(table);
			if(t==null){
				t=table.getDeclaredConstructor(DungeonFloor.class).newInstance(f);
				t.modify();
				tables.put(table,t);
			}
			return (K)t;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/** As {@link #clone()} but updates {@link #floor}. */
	@Override
	public Tables clone(){
		try{
			var c=(Tables)super.clone();
			c.tables=new HashMap<>(tables.size());
			for(var t:tables.keySet())
				c.tables.put(t,tables.get(t).clone());
			return c;
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(){
		String tables="";
		for(Class<? extends Table> table:this.tables.keySet())
			tables+=this.tables.get(table)+"\n\n";
		return tables;
	}
}
