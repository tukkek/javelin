package javelin.model.world;

import java.io.Serializable;
import java.util.List;

import javelin.Javelin;
import javelin.controller.scenario.Campaign;
import javelin.model.unit.Squad;
import javelin.view.screen.WorldScreen;

/**
 * Instances abstract the time of day. Also offers {@link World}-time helper
 * methods.
 *
 * @author alex
 */
public class Period implements Serializable,javelin.model.Cloneable{
	/** 6AM to noon. */
	public static final Period MORNING=new Period("Morning",6,12);
	/** Noon to 6PM. */
	public static final Period AFTERNOON=new Period("Afternoon",12,18);
	/** 6PM to midnight. */
	public static final Period EVENING=new Period("Evening",18,24);
	/** Midnight to 6PM. */
	public static final Period NIGHT=new Period("Night",0,6);
	/** All periods of the day. */
	public static final List<Period> ALL=List.of(MORNING,AFTERNOON,EVENING,NIGHT);

	String name;
	/** Inclusive. */
	int from;
	/** Exclusive. */
	int to;

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
	 *         UNIX time, "epoch").
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
		if(Javelin.app.fight!=null) return Javelin.app.fight.period;
		for(var p:ALL)
			if(p.is()) return p;
		throw new RuntimeException("Unknown hour: "+gethour());
	}
}
