package javelin.controller.generator.feature;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javelin.controller.exception.RestartWorldGeneration;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.view.screen.WorldScreen;

/**
 * One of these per feature.
 *
 * @see FeatureGenerator#spawn(float, boolean)
 * @author alex
 */
public class Frequency implements Serializable{
	/** {@link #absolute} (but probabilistic) value for {@link #chance}. */
	public static final float DAILY=WorldScreen.SPAWNPERIOD;
	/** {@link #absolute} (but probabilistic) value for {@link #chance}. */
	public static final float WEEKLY=DAILY/7;
	/** {@link #absolute} (but probabilistic) value for {@link #chance}. */
	public static final float MONTHLY=DAILY/30;
	/** {@link #absolute} (but probabilistic) value for {@link #chance}. */
	public static final float SEASONALY=DAILY/100;
	/** {@link #absolute} (but probabilistic) value for {@link #chance}. */
	public static final float YEARLY=DAILY/400;

	/**
	 * When instantiated this is the relative chance of this feature being
	 * spawned. This is later modified to represent a percentage value relative to
	 * all other non-{@link #absolute} entries.
	 */
	public float chance=1;
	/**
	 * If <code>true</code> then {@link #chance} won't be altered. That does not
	 * make it an absolute per-day chance.
	 *
	 * @see FeatureGenerator#spawn(float, boolean)
	 */
	public boolean absolute=false;
	/**
	 * If <code>true</code> this feature will be eligible for instantiation both
	 * during {@link World} generation and during normal gameplay. If
	 * <code>false</code> will only be spawned during normal gameplay.
	 */
	public boolean starting=true;
	/**
	 * If not <code>null</code> won't spawn any more of these feature if their
	 * maximum allowerd number per {@link World} has been reached already.
	 */
	public Integer max=null;
	/**
	 * The starting number of instances of this feature to generate on the world
	 * map.
	 *
	 * Default seed is always at least 1 so that debug statistics won't botch.
	 */
	public Integer seeds=1;

	/** Construct with default values. */
	public Frequency(){
		super();
	}

	/**
	 * @param chance Overrides default {@link #chance} value.
	 */
	public Frequency(float chance){
		this.chance=chance;
	}

	/**
	 * Constructor.
	 *
	 * @param chance See {@link #chance};
	 * @param absolute See {@link #absolute}.
	 * @param starting
	 */
	public Frequency(float chance,boolean absolute,boolean starting){
		this(chance);
		this.absolute=absolute;
		this.starting=starting;
	}

	/**
	 * @return By default a new instance of the given {@link Actor} clss, using
	 *         Java Reflection.
	 */
	public Actor generate(Class<? extends Actor> feature){
		try{
			return feature.getDeclaredConstructor().newInstance();
		}catch(RestartWorldGeneration e){
			throw e;
		}catch(InvocationTargetException e){
			Throwable cause=e.getTargetException();
			if(cause instanceof RestartWorldGeneration)
				throw(RestartWorldGeneration)cause;
			throw new RuntimeException(e);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * By default just calls {@link #generate(Class)} and places the result.
	 *
	 * @param feature Feature type.
	 * @see Actor#place()
	 */
	public void seed(Class<? extends Actor> feature){
		for(int i=0;i<seeds;i++)
			generate(feature).place();
	}
}