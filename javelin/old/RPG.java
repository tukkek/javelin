package javelin.old;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.ai.BattleAi;

public class RPG{
	public static Random rand=new Random();

	public static float random(){
		return rand.nextFloat();
	}

	/**
	 * Random number from zero to s-1
	 *
	 * @param s Upper bound (excluded)
	 * @return
	 */
	public static final int r(final int s){
		if(s<=0) return 0;
		return rand.nextInt(s);
	}

	/**
	 * @return A random number uniformly distributed in [n1, n2] range. It is
	 *         allowed to have so n1 > n2, or n1 < n2, or n1 == n2.
	 */
	public static final int r(final int n1,final int n2){
		return Math.min(n1,n2)+rand.nextInt(Math.max(n1,n2)-Math.min(n1,n2)+1);
	}

	public static int rolldice(final int number,final int sides){
		int total=0;
		for(int i=0;i<number;i++)
			total+=r(1,sides);
		return total;
	}

	public static <K> K pick(final List<K> list){
		return list.get(RPG.r(list.size()));
	}

	public static boolean chancein(int x){
		return RPG.r(1,x)==1;
	}

	/**
	 * Used to return numbers that average 0, useful for adding a small random
	 * factor to things that would otherwise be boring and 100% predictable. For
	 * example, if you input 4, it will return 1d4-1d4, which tends to 0 but could
	 * actually return anywhere in the range [-3,+3].
	 *
	 * A negative value provided is turned into a positive one for all intents and
	 * purposes. An input of zero will also return zero.
	 *
	 * @param sides Given a die X...
	 * @return the result of 1dX - 1dX.
	 * @deprecated See {@link #randomize()}.
	 */
	@Deprecated
	public static int randomize(int sides){
		if(sides==0) return sides;
		if(sides<0) sides=Math.abs(sides);
		return r(1,sides)-r(1,sides);
	}

	/**
	 * This improves on {@link #randomize(int)} in two levels: first syntactically
	 * you don't need the <code>x + randomize(x)</code> idiom, which is awkward
	 * and very error prone; second, it is not predictable or deterministic as it
	 * can scale up and down infinitely (although increasingly unlikely) - while
	 * the original method has a once-derived defined range.
	 *
	 * TODO replace old users of {@link #randomize(int)}, test and remove it.
	 *
	 * Note that the semantics of this method are different from
	 * {@link #randomize(int)}, this is not a syntatic-sugar signature!
	 *
	 * @param value The original value, which will be randomized between one and
	 *          an infinite amount of times. Each pass is a <code>+1dx-1dX</code>
	 *          sum, where X equals the value itself.
	 * @param min Will cap the result to this lower bound.
	 * @param max Will cap the result to this higher bound.
	 * @return The original value plus the sum of all random iterations it went
	 *         through.
	 */
	public static int randomize(int value,int min,int max){
		if(Javelin.DEBUG&&min>max) throw new InvalidParameterException();
		if(value<min) return min;
		if(value>max) return max;
		if(value==0) return 0;
		value=Math.abs(value);
		var result=value+r(1,value)-r(1,value);
		if(result<min) return min;
		if(result>max) return max;
		return RPG.chancein(2)?result:randomize(result,min,max);
	}

	/**
	 * Useful for handling typical cases in {@link BattleAi}-related logic, since
	 * it only allows for true randomness in a few key points due to performance
	 * considerations. Uses float for parameters since they'd have to be convered
	 * anyways.
	 *
	 * TODO change all project int to long, all float to double
	 *
	 * @param dice Given this number of dice...
	 * @param size ...and this number of faces per dice...
	 * @return ... the most likely outcome of this roll.
	 */
	public static int average(double dice,int size){
		size+=1;
		return Math.round(Math.round(dice*size/2.0));
	}

	/**
	 * @return The same input list, but shuffled with
	 *         {@link Collections#shuffle(List)}.
	 */
	public static <K extends List<?>> K shuffle(K list){
		Collections.shuffle(list);
		return list;
	}

	/** @return Same as {@link #pick(List)}. */
	public static <K> K pick(Set<K> set){
		return pick(new ArrayList<>(set));
	}

	/** @return x elements from list (will pad to list size). */
	public static <K> List<K> pick(List<K> l,int amount){
		if(amount==0) return Collections.EMPTY_LIST;
		return shuffle(new ArrayList<>(l)).subList(0,Math.min(amount,l.size()));
	}

	/** @return Roll a x-sided dice y times and return the highest value. */
	public int advantage(int sides,int rolls){
		var result=Integer.MIN_VALUE;
		for(var i=0;i<rolls;i++)
			result=Math.max(result,r(1,sides));
		return result;
	}

	/** @return Roll a x-sided dice y times and return the lowest value. */
	public int disadvantage(int sides,int rolls){
		var result=Integer.MAX_VALUE;
		for(var i=0;i<rolls;i++)
			result=Math.min(result,r(1,sides));
		return result;
	}
}