package javelin.model.world.location.fortification;

import java.util.ArrayList;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * A guarded place is a world feature that has a garrison army since the
 * bieggining of the game. This is meant to expand the number of places to be
 * explored, while also offering non-scaled (fixed difficulty) encounters to
 * offset the scaled nature of {@link RandomEncounter}s and most other game
 * battles.
 *
 * @author alex
 * @see Location#garrison
 */
public abstract class Fortification extends Location {
	/** To be shown on a successful {@link Skills#knowledge} check. */
	public String descriptionknown;
	/** To be shown on a failed {@link Skills#knowledge} check. */
	protected String descriptionunknown;
	/**
	 * The decided encounter level in the given range. See
	 * {@link #Fortification(String, String, int, int)}.
	 */
	public Integer targetel = null;
	public int minlevel;
	public int maxlevel;
	/** TODO There is certainly a better way to do this. */
	public boolean generategarrison = true;
	/**
	 * Will not have a garrison if {@link Scenario#clearlocations} is
	 * <code>true</code>.
	 */
	public boolean clear = false;
	/**
	 * If not <code>null</code> will use this for
	 * {@link #generategarrison(int, int)}.
	 */
	protected Terrain terrain = null;
	/** Neutral location don't generate a garrison. */
	protected boolean neutral = false;

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
	 * @param minlevel
	 *            Minimum difficulty. Will be converted into a proper Upper
	 *            Krust EL.
	 * @param maxlevel
	 *            Maximum difficulty. Will be converted into a proper Upper
	 *            Krust EL.
	 */
	public Fortification(String descriptionknown, String descriptionunknown,
			int minlevel, int maxlevel) {
		super(null);
		this.minlevel = minlevel;
		this.maxlevel = maxlevel;
		realm = Realm.AIR;// see #place()
		discard = false;
		allowentry = false;
		this.descriptionknown = descriptionknown;
		this.descriptionunknown = descriptionunknown;
		vision = 1;
	}

	/**
	 * The default implementation generates a {@link Location#garrison}
	 * according to the location.
	 *
	 * Called during construction and responsible for setting {@link #targetel}.
	 *
	 * @param minlevel
	 *            See {@link #minlevel}.
	 * @param maxlevel
	 *            See {@link #maxlevel}
	 */
	protected void generategarrison(int minlevel, int maxlevel) {
		if (neutral) {
			raiselevel(0);
			capture();
			return;
		}
		while (garrison.isEmpty()) {
			try {
				int el = RPG.r(minlevel, maxlevel);
				ArrayList<Terrain> terrains = new ArrayList<Terrain>(1);
				terrains.add(terrain == null ? Terrain.get(x, y) : terrain);
				garrison.addAll(EncounterGenerator.generate(el, terrains));
				targetel = el;
			} catch (GaveUp e) {
				continue;
			}
		}
	}

	public void raiselevel(int bonus) {
		minlevel += bonus;
		maxlevel += bonus;
		targetel = RPG.r(minlevel, maxlevel);
	}

	private Actor findclosest(Class<? extends Actor> type) {
		Actor closest = null;
		for (Actor a : World.getall(type)) {
			if (closest == null || Walker.distance(a.x, a.y, x, y) < Walker
					.distance(closest.x, closest.y, x, y)) {
				closest = a;
			}
		}
		return closest;
	}

	@Override
	public Integer getel(int attackerel) {
		return ChallengeCalculator.calculateel(garrison);
	}

	@Override
	public String toString() {
		if (targetel == null || !ishostile()) {
			return descriptionknown;
		}
		int knowledge = Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE);
		return knowledge >= 10 + targetel ? descriptionknown : descriptionunknown;
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
		return RewardCalculator.getgold(ChallengeCalculator.eltocr(el + 1));
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

	@Override
	public void place() {
		super.place();
		if (realm != null) {
			Actor town = findclosest(Town.class);
			realm = town == null ? null : ((Town) town).realm;
		}
		if (generategarrison && garrison.isEmpty()) {
			generategarrison(minlevel, maxlevel);
			generategarrison = false;
			if (World.scenario.clearlocations && clear) {
				capture();
			}
		}
		if (!ishostile()) {
			realm = null;
		}
	}

	@Override
	public void rename(String name) {
		super.rename(name);
		descriptionknown = name;
		descriptionunknown = name;
	}
}