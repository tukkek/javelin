package javelin.model.world.place.guarded;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.encounter.EncounterGenerator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.walker.Walker;
import javelin.model.unit.Skills;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * A guarded place is a world feature that has a garrison army since the
 * bieggining of the game. This is meant to expand the number of places to be
 * explored, while also offering non-scaled (fixed difficulty) encounters to
 * offset the scaled nature of {@link RandomEncounter}s and most other game
 * battles.
 * 
 * @author alex
 * @see WorldPlace#garrison
 */
public abstract class GuardedPlace extends WorldPlace {
	static final boolean DEBUG = false;
	/** To be shown on a successful {@link Skills#knowledge} check. */
	protected String descriptionknown;
	/** To be shown on a failed {@link Skills#knowledge} check. */
	protected String descriptionunknown;
	/**
	 * The decided encounter level in the given range. See
	 * {@link #GuardedPlace(String, String, int, int)}.
	 */
	protected Integer targetel = null;

	/**
	 * Generates a guarded location based on a difficulty range. The difficulty
	 * here is taken as the level of 4 characters that would be a fair combat to
	 * the current garrison. This enables subclasses to use the classic D&D
	 * tiers, while avoiding absolute predictability:
	 * 
	 * <br/>
	 * Levels 1-5 (low level)
	 * 
	 * <br/>
	 * Levels 6-10 (medium level)
	 * 
	 * <br/>
	 * Levels 11-15 (high level)
	 * 
	 * <br/>
	 * Levels 16-20 (legendary level)
	 * 
	 * <br/>
	 * Levels 21+ (epic, not yet implemented)
	 * 
	 * @param minel
	 *            Minimum difficulty. Will be converted into a proper Upper
	 *            Krust EL.
	 * @param maxel
	 *            Maximum difficulty. Will be converted into a proper Upper
	 *            Krust EL.
	 */
	public GuardedPlace(String descriptionknown, String descriptionunknown,
			int minel, int maxel) {
		super(null);
		WorldActor town = findclosest(Town.class);
		realm = town == null ? null : ((Town) town).realm;
		discard = false;
		allowentry = false;
		this.descriptionknown = descriptionknown;
		this.descriptionunknown = descriptionunknown;
		generategarrison(calculateel(minel), calculateel(maxel));
		if (DEBUG) {
			garrison.clear();
		}
	}

	/**
	 * The default implementation generates a {@link WorldPlace#garrison}
	 * according to the location.
	 * 
	 * Called during construction and responsible for setting {@link #targetel}.
	 * 
	 * @param minel
	 *            Minimum encounter level.
	 * @param maxel
	 *            Maximum encounter level.
	 */
	protected void generategarrison(int minel, int maxel) {
		while (garrison.isEmpty()) {
			try {
				int el = RPG.r(minel, maxel);
				garrison.addAll(
						EncounterGenerator.generate(el, Javelin.terrain(x, y)));
				targetel = new Integer(el);
			} catch (GaveUpException e) {
				continue;
			}
		}
	}

	private WorldActor findclosest(Class<? extends WorldActor> type) {
		WorldActor closest = null;
		for (WorldActor a : WorldActor.getall(type)) {
			if (closest == null || Walker.distance(a.x, a.y, x, y) < Walker
					.distance(closest.x, closest.y, x, y)) {
				closest = a;
			}
		}
		return closest;
	}

	static int calculateel(int el) {
		return ChallengeRatingCalculator.elFromCr(el * 4) - 4;
	}

	@Override
	protected Integer getel(int attackerel) {
		return ChallengeRatingCalculator.calculateElSafe(garrison);
	}

	@Override
	public String toString() {
		if (targetel == null) {
			return descriptionknown;
		}
		return Squad.active.know() >= targetel ? descriptionknown
				: descriptionunknown;
	}

	/**
	 * @return The amount of gold that should be given if this in case of a
	 *         {@link #pillage()}.
	 */
	public int getspoils() {
		return getspoils(targetel);
	}

	/**
	 * @param el
	 *            Will use an encounter level 1 higher than this.
	 * @return See {@link #getspoils()}.
	 */
	static public int getspoils(Integer el) {
		return RewardCalculator
				.getgold(ChallengeRatingCalculator.eltocr(el + 1)[0]);
	}

	/**
	 * Destroys this place, adding a certain amount of gold to the active
	 * {@link Squad}. This is mostly allowed as to allow a less strategic
	 * gameplay for players who want to just pillage and move on, without using
	 * locations for their strategic value.
	 * 
	 * @see #getspoils()
	 */
	public void pillage() {
		Squad.active.gold += getspoils();
		remove();
	}
}