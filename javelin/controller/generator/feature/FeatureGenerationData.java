package javelin.controller.generator.feature;

import javelin.model.world.World;
import javelin.model.world.WorldActor;

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
	public Integer max = null;
	/**
	 * The starting number of instances of this feature to generate on the world
	 * map.
	 */
	public Integer seeds = 0;

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
	public FeatureGenerationData(float chance, Integer max, Integer seeds) {
		this(chance);
		this.max = max;
		this.seeds = seeds;
	}

	/**
	 * @return By default a new instance of the given {@link WorldActor} clss,
	 *         using {@link Class#newInstance()}.
	 */
	public WorldActor generate(Class<? extends WorldActor> feature) {
		try {
			return feature.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * By default just calls {@link #generate(Class)} and places the result.
	 * 
	 * @param feature
	 *            Feature type.
	 * @see WorldActor#place()
	 */
	public void seed(Class<? extends WorldActor> feature) {
		for (int i = 0; i < seeds; i++) {
			generate(feature).place();
		}
	}
}