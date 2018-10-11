package javelin.controller.table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import javelin.old.RPG;

public class Table implements Serializable,Cloneable{
	class RowData implements Serializable,Cloneable{
		int min;
		int max;
		int variance;

		public RowData(int min,int max,int variance){
			super();
			this.min=min;
			this.max=max;
			this.variance=variance;
			if(javelin.Javelin.DEBUG) assert min<=max;
		}

		@Override
		public RowData clone() throws CloneNotSupportedException{
			return (RowData)super.clone();
		}
	}

	HashMap<Object,Integer> rows=new HashMap<>();
	HashMap<Object,RowData> data=new HashMap<>();

	/**
	 * If adding a preexisting result, will instead raise its current chances and
	 * ceiling by the given ceiling parameter.
	 *
	 * @param row Table result.
	 * @param min Chance floor.
	 * @param max Chance ceiling.
	 * @param variance How much to vary in the given range on {@link #modify()}.
	 */
	public void add(Object row,int min,int max,int variance){
		RowData previous=data.get(row);
		if(previous==null){
			rows.put(row,RPG.r(min,max));
			data.put(row,new RowData(min,max,variance));
		}else{
			rows.put(row,rows.get(row)+max);
			previous.max+=max;
		}
	}

	/**
	 * Adds with default variance.
	 *
	 * @see #add(Object, int, int, int)
	 */
	public void add(Object row,int min,int max){
		add(row,min,max,Math.max(1,(min+max)/2));
	}

	/**
	 * Adds with default minimum and variance.
	 *
	 * @see #add(Object, int, int, int)
	 */
	public void add(Object row,int max){
		add(row,1,max);
	}

	@Override
	public Table clone(){
		try{
			Table clone=(Table)super.clone();
			clone.data=new HashMap<>(data);
			clone.rows=new HashMap<>(rows);
			for(Object key:data.keySet())
				clone.data.put(key,data.get(key).clone());
			clone.modify();
			return clone;
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	public void modify(){
		for(Object key:data.keySet()){
			RowData d=data.get(key);
			if(d.variance==0) continue;
			int current=rows.get(key);
			current+=RPG.randomize(d.variance+1);
			if(current>d.max)
				current=d.max;
			else if(current<d.min) current=d.min;
			rows.put(key,current);
		}
	}

	public Object roll(){
		int roll=RPG.r(1,getchances());
		for(Object row:rows.keySet()){
			roll-=rows.get(row);
			if(roll<=0) return row;
		}
		throw new RuntimeException("#brokentable "+this);
	}

	public int getchances(){
		int total=0;
		for(Integer chances:rows.values())
			total+=chances;
		return total;
	}

	@Override
	public String toString(){
		Set<Object> keys=rows.keySet();
		int format=Integer.toString(getchances()+keys.size()).length();
		int current=1;
		int chances=getchances();
		String table=getClass().getSimpleName().toString()+"\n";
		for(Object row:keys){
			Integer chance=rows.get(row);
			int ceiling=current+chance-1;
			int percent=100*chance/chances;
			table+=format(current,format)+"-"+format(ceiling,format)+" "+row+" ("
					+percent+"%)\n";
			current=ceiling;
			current+=1;
		}
		return table;
	}

	String format(int chances,int length){
		return String.format("%0"+length+"d",chances);
	}

	public Integer rollnumber(){
		return (Integer)roll();
	}

	public boolean rollboolean(){
		return (Boolean)roll();
	}
}
