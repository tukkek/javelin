package javelin.controller.generator.feature;

import javelin.controller.exception.RestartWorldGeneration;
import javelin.model.world.Actor;
import javelin.model.world.World;

/**
 * One of these per feature.
 *
 * @see FeatureGenerator#spawn(float, boolean)
 * @author alex
 */
public class FeatureGenerationData {
	/**
	 * When instantiated this is the relative chance of this feature being
	 * spawned. This is later modified.
	 */
	public float chance = 1;
	/**
	 * If <code>true</code> then {@link #chance} won't be altered by
	 * {@link FeatureGenerator#convertchances()}.
	 */
	public boolean absolute = false;
	/**
	 * If <code>true</code> this feature will be eligible for instantiation both
	 * during {@link World} generation and during normal gameplay. If
	 * <code>false</code> will only be spawned during normal gameplay.
	 */
	public boolean starting = true;
	/**
	 * If not <code>null</code> won't spawn any more of these feature if there
	 * maximum allowerd number per {@link World} has been reached already.
	 */
	public Integer max = 9;
	/**
	 * The starting number of instances of this feature to generate on the world
	 * map.
	 * 
	 * Make seed it's always at least 1 so that debug statistics won't botch.
	 */
	public Integer seeds = 1;

	/** Construct with default values. */
	public FeatureGenerationData() {
		return;
	}

	/**
	 * @param chance
	 *            Overrides default {@link #chance} value.
	 */
	public FeatureGenerationData(float chance) {
		this.chance = chance;
	}

	/**
	 * @param chancep
	 *            Overrides {@link #chance}
	 * @param absolutep
	 *            Overrides {@link #absolute}.
	 * @param startingp
	 *            Overrides {@link #starting}.
	 */
	public FeatureGenerationData(float chancep, boolean absolutep,
			boolean startingp) {
		this(chancep);
		absolute = absolutep;
		starting = startingp;
	}

	/**
	 * @param chance
	 *            Overrides {@link #chance}.
	 * @param max
	 *            Overrides {@link #max}.
	 * @param seeds
	 *            Overrides {@link #seeds}.
	 */
	public FeatureGenerationData(float chance, Integer max, int seeds) {
		this(chance);
		this.max = max;
		this.seeds = Math.max(1, seeds);
	}

	public FeatureGenerationData(Integer object) {
		max = object;
	}

	/**
	 * @return By default a new instance of the given {@link Actor} clss, using
	 *         {@link Class#newInstance()}.
	 */
	public Actor generate(Class<? extends Actor> feature) {
		try {
			return feature.newInstance();
		} catch (RestartWorldGeneration e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * By default just calls {@link #generate(Class)} and places the result.
	 *
	 * @param feature
	 *            Feature type.
	 * @see Actor#place()
	 */
	public void seed(Class<? extends Actor> feature) {
		for (int i = 0; i < seeds; i++) {
			generate(feature).place();
		}
	}
}