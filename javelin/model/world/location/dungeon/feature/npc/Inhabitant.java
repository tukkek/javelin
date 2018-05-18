package javelin.model.world.location.dungeon.feature.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.table.dungeon.DungeonFeatureModifier;
import javelin.controller.upgrade.SkillUpgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.temple.TempleDungeon;
import tyrant.mikera.engine.RPG;

/**
 * A friendly (or at least neutral) {@link Monster} living inside a
 * {@link Dungeon}. Tries to utilize one of the creatures in
 * {@link Dungeon#encounters} but will generate a new one if necessary.
 *
 * @author alex
 */
public abstract class Inhabitant extends Feature {
	/** @see Inhabitant */
	public Combatant inhabitant;

	/**
	 * Will advance any selected {@link Monster} up to this level using
	 * {@link NpcGenerator}.
	 *
	 * @see #select()
	 */
	float crmin;
	/**
	 * Used as an upper bound if having to generate a monsster.
	 */
	float crmax;
	/**
	 * Between 0-100% normal treasure, to prevent players from blindly attacking
	 * them for treasure. However, allows players to recuperate any gold they
	 * provide the NPC if they choose to do so (as long as the value is kept
	 * up-to-date).
	 */
	protected int gold;
	/**
	 * DC 10 + {@link Dungeon#level} + {@link DungeonFeatureModifier}.
	 * Subclasses may alter them as needed.
	 */
	protected int diplomacydc;

	public Inhabitant(int xp, int yp, float crmin, float crmax) {
		super(xp, yp, null);
		this.crmin = crmin;
		this.crmax = crmax;
		remove = false;
		inhabitant = select();
		avatarfile = inhabitant.source.avatarfile;
		new SkillUpgrade(Skill.DIPLOMACY).upgrade(inhabitant);
		diplomacydc = inhabitant.taketen(Skill.DIPLOMACY)
				+ Dungeon.gettable(DungeonFeatureModifier.class).rollmodifier();
		int d100 = RPG.r(0, 100);
		gold = RewardCalculator.getgold(inhabitant.source.cr) * d100 / 100;
	}

	/**
	 * Both CR parameters may be stretched internally to find a suitable
	 * candidate.
	 *
	 * @return An intelligent {@link Monster}, which is valid even if
	 *         {@link #dungeon} is a {@link TempleDungeon}. If it can't find one
	 *         in {@link Dungeon#encounters}, generates one instead.
	 */
	public Combatant select() {
		HashSet<String> invalid = new HashSet<String>();
		for (Combatant c : Dungeon.active.rasterizenecounters()) {
			Monster m = c.source;
			String name = m.name;
			if (invalid.contains(name) || !validate(m)) {
				invalid.add(name);
				continue;
			}
			Combatant npc = NpcGenerator.generatenpc(m, crmin);
			if (npc != null) {
				return npc;
			}
		}
		return new Combatant(generate(crmin, crmax), true);
	}

	Monster generate(float crmin, float crmax) {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for (Float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (crmin <= cr && cr <= crmax) {
				candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		}
		Collections.shuffle(candidates);
		for (Monster m : candidates) {
			if (validate(m)) {
				return m;
			}
		}
		return generate(crmin - 1, crmax + 1);
	}

	/**
	 * @return <code>true</code> if the given Monster fits the {@link #dungeon}
	 *         context.
	 */
	protected boolean validate(Monster m) {
		if (!m.think(-1)) {
			return false;
		}
		TempleDungeon td = (TempleDungeon) (Dungeon.active instanceof TempleDungeon
				? Dungeon.active : null);
		List<Monster> aslist = Arrays.asList(new Monster[] { m });
		return td == null || td.temple.validate(aslist);
	}
}