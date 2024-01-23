package javelin.controller.table;

import java.io.OptionalDataException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javelin.Javelin;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.old.RPG;

/**
 * Inspired by traditional tabletop RPG "random tables", a Table holds a number
 * of rows - one of which being picked randomly when the table is used. These
 * tables are self-mutation for two purposed: 1. so that each instances of the
 * table are never pre-defined but instead only descrived as general guidelines
 * to what their contents should be and 2. so that they can be copied and
 * mutated (for example, between levels of the same {@link DungeonFloor}) -
 * which will in turn keep a cohesive theme while still having mutation between
 * levels.
 *
 * For example: a table describiing how many {@link Trap}s (or serets
 * {@link Door}s) should be in a dungeon starts as a guideline but upon being
 * instantiated, could result in a high or low amount of these features. Further
 * copying and mutating these tables inside the same dungeon would continue the
 * trend, but still providing a chance that traps and secret doors could beecome
 * more or less present in later levels, while still wokring under the confines
 * of a balanced experience.
 *
 * This system produces a very high amount of variability while keeping
 * consistency to predefined boundaries and for the same family of tables (being
 * very unlikely but póssible that a dungeon floor with no traps, for example,
 * would lead to a floor below with an extreme amount of traps).
 *
 * The implementation of these tables is highly generic and can be used to
 * deliver any sort of object, including primitives.
 *
 * @author alex
 */
public class Table implements Serializable,Cloneable{
  class RowData implements Serializable,Cloneable{
    int min;
    int max;
    int variance;

    public RowData(int min,int max,int variance){
      if(Javelin.DEBUG&&min>max) throw new InvalidParameterException();
      this.min=min;
      this.max=max;
      this.variance=variance;
    }

    @Override
    public RowData clone() throws CloneNotSupportedException{
      return (RowData)super.clone();
    }
  }

  /**
   * {@link HashMap} was throwing {@link OptionalDataException} during
   * deserialization.
   */
  Map<Object,Integer> rows=new Hashtable<>();
  Map<Object,RowData> data=new Hashtable<>();

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
    var previous=data.get(row);
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
      var c=(Table)super.clone();
      c.data=new Hashtable<>(data);
      c.rows=new Hashtable<>(rows);
      for(var k:data.keySet()) c.data.put(k,data.get(k).clone());
      c.modify();
      return c;
    }catch(CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  /** Mutates a table. */
  public void modify(){
    for(Object key:data.keySet()){
      var d=data.get(key);
      if(d.variance==0) continue;
      int current=rows.get(key);
      current+=RPG.randomize(d.variance+1);
      if(current>d.max) current=d.max;
      else if(current<d.min) current=d.min;
      rows.put(key,current);
    }
  }

  /**
   * @return One of the table rows, randomly selected or <code>null</code> if
   *   table is empty.
   */
  public Object roll(){
    var roll=RPG.r(1,getchances());
    for(Object row:rows.keySet()){
      roll-=rows.get(row);
      if(roll<=0) return row;
    }
    return null;
  }

  /** @return The total amount of rows in this table. */
  public int getchances(){
    var total=0;
    for(Integer chances:rows.values()) total+=chances;
    return total;
  }

  @Override
  public String toString(){
    var keys=rows.keySet();
    var format=Integer.toString(getchances()+keys.size()).length();
    var current=1;
    var chances=getchances();
    var table=getClass().getSimpleName().toString()+"\n";
    for(Object row:keys){
      var chance=rows.get(row);
      var ceiling=current+chance-1;
      var percent=100*chance/chances;
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

  /** As {@link #roll()} but to be used for numeric tables. */
  public Integer rollnumber(){
    return (Integer)roll();
  }

  /** As {@link #roll()} but to be used for boolean tables. */
  public boolean rollboolean(){
    return (Boolean)roll();
  }

  /** @return <code>true</code> if there are zero rows. */
  public boolean isempty(){
    return getchances()==0;
  }

  /** @return All unique rows, without any chance information. */
  public Set<Object> getrows(){
    return rows.keySet();
  }

  /**
   * Adds object with arbitrary parameters. Recommended only when adding all
   * table objects using this method, resulting in equal initial odds.
   */
  public void add(Object o){
    add(o,10);
  }
}
