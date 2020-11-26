package javelin.controller;

import java.util.ArrayList;

import javelin.Debug;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.world.Season;
import javelin.old.RPG;

/**
 * Manages game weather. Current types of weather are {@link #CLEAR},
 * {@link #RAIN} and {@link #STORM}.
 *
 * @author alex
 */
public class Weather{
	public static final int CLEAR=0;
	public static final int RAIN=1;
	public static final int STORM=2;
	public static final Integer[] DISTRIBUTION=new Integer[]{CLEAR,CLEAR,RAIN,STORM};

	static final double[] RATIO=new double[]{0.0,.1,.5};

	public static int current;

	static{
		read(0);
	}

	/**
	 * Changes the weather, possibly.
	 *
	 * @see Season#getweather()
	 */
	public static void weather(){
		if(Debug.weather!=null){
			current=read(0);
			return;
		}
		int roll=RPG.r(0,DISTRIBUTION.length-1)+Season.current.getweather();
		if(roll<0)
			roll=0;
		else if(roll>=DISTRIBUTION.length) roll=DISTRIBUTION.length-1;
		roll=DISTRIBUTION[roll];
		if(roll>current)
			current+=1;
		else if(roll<current) current-=1;
	}

	public static void flood(){
		int level=Fight.current.flood();
		final double r=RATIO[level];
		if(r==0.0) return;
		final ArrayList<Square> clear=new ArrayList<>();
		final BattleState state=Fight.state;
		for(Square[] line:state.map)
			for(Square s:line)
				if(!s.obstructed&&!s.blocked) clear.add(s);
		double spots=clear.size()*r;
		for(double i=0.0;i<spots&&!clear.isEmpty();i+=1.0){
			final int index=RPG.r(1,clear.size())-1;
			Square s=clear.get(index);
			clear.remove(index);
			s.flooded=true;
		}
	}

	public static int read(int nowp){
		if(Debug.weather==null) return nowp;
		if(Debug.weather.equals("dry")) return CLEAR;
		if(Debug.weather.equals("rain")) return RAIN;
		if(Debug.weather.equals("storm")) return STORM;
		throw new RuntimeException("Unknown weather: "+Debug.weather);
	}
}
