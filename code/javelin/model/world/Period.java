package javelin.model.world;

import java.io.Serializable;
import java.util.List;

import javelin.controller.content.fight.Fight;
import javelin.model.Cloneable;
import javelin.model.unit.Squad;
import javelin.view.screen.WorldScreen;

/// Instances represent day-period. Has {@link World}-time helper methods.
public class Period implements Serializable,Cloneable,Comparable<Period>{
  /** 6AM to noon. */
  public static final Period MORNING=new Period("Morning",6,12);
  /** Noon to 6PM. */
  public static final Period DAY=new Period("Day",12,18);
  /** 6PM to midnight. */
  public static final Period EVENING=new Period("Evening",18,24);
  /** Midnight to 6PM. */
  public static final Period NIGHT=new Period("Night",0,6);
  /** All periods of the day. */
  public static final List<Period> ALL=List.of(MORNING,DAY,EVENING,NIGHT);

  String name;
  /** Inclusive. */
  public int from;
  /** Exclusive. */
  public int to;

  Period(String name,int from,int to){
    this.name=name;
    this.from=from;
    this.to=to;
  }

  @Override
  public int hashCode(){
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o){
    var p=(Period)o;
    return name.equals(p.name);
  }

  @Override
  public String toString(){
    return name;
  }

  @Override
  public Period clone(){
    try{
      return (Period)super.clone();
    }catch(CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  /** @return <code>true</code> if given hour is within this period. */
  public boolean is(long hour){
    return from<=hour&&hour<to;
  }

  /** @return As {@link #is(long)} but for current {@link World} time. */
  public boolean is(){
    return is(gethour());
  }

  /**
   * @return Cumulative hour since the start of the {@link Campaign} (similar to
   *   UNIX time, "epoch").
   */
  public static long gettime(){
    return Squad.active==null?Math.round(WorldScreen.lastday*24)
        :Squad.active.gettime();
  }

  /**
   * If {@link Squad#active} is <code>null</code> for any reason, will return
   * zero.
   *
   * @return Hour of the day, from 0 to 23.
   */
  public static long gethour(){
    return Squad.active==null?0:gettime()%24;
  }

  /** The current time of day in the game {@link World}. */
  public static Period now(){
    if(Fight.current!=null) return Fight.current.period;
    for(var p:ALL) if(p.is()) return p;
    throw new RuntimeException("Unknown hour: "+gethour());
  }

  @Override
  public int compareTo(Period o){
    return Integer.compare(from,o.from);
  }
}
