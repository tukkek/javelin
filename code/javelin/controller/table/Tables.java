package javelin.controller.table;

import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javelin.model.world.location.dungeon.DungeonFloor;

public class Tables implements Serializable,Cloneable{
	/**
	 * {@link HashMap}was throwing {@link OptionalDataException} on
	 * de-serialization.
	 */
	Map<Class<? extends Table>,Table> tables=new Hashtable<>();

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
			c.tables=new Hashtable<>(tables.size());
			//using keySet() here produces the weirdest bug, don't
			for(var t:tables.entrySet())
				c.tables.put(t.getKey(),t.getValue().clone());
			return c;
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(){
		String tables="";
		for(var table:this.tables.keySet())
			tables+=this.tables.get(table)+"\n\n";
		return tables;
	}
}
