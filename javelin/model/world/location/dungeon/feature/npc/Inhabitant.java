package javelin.model.world.location.dungeon.feature.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.table.dungeon.DungeonFeatureModifier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
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
	protected int basedc = 10 + Dungeon.active.level
			+ Dungeon.gettable(DungeonFeatureModifier.class).rollmodifier();

	public Inhabitant(int xp, int yp) {
		super(xp, yp, null);
		remove = false;
		inhabitant = select();
		avatarfile = inhabitant.source.avatarfile;
		int d100 = RPG.r(0, 100);
		gold = RewardCalculator.getgold(inhabitant.source.cr) * d100 / 100;
	}

	/**
	 * Both CR parameters may be stretched internally to find a suitable
	 * candidate.
	 *
	 * @param crmin
	 *            Will advance any selected monster up to this level using
	 *            {@link NpcGenerator}.
	 * @param crmax
	 *            Used as an upper bound if having to generate a monsster.
	 * @return An intelligent {@link Monster}, which is valid even if
	 *         {@link #dungeon} is a {@link TempleDungeon}. If it can't find one
	 *         in {@link Dungeon#encounters}, generates one instead.
	 */
	public Combatant select(int crmin, int crmax) {
		HashSet<String> invalid = new HashSet<String>();
		for (Combatant c : Dungeon.active.rasterizenecounters()) {
			String name = c.source.name;
			if (invalid.contains(name) || !validate(c.source)) {
				invalid.add(name);
				continue;
			}
			Combatant npc = NpcGenerator.generatenpc(c.source, crmin);
			if (npc != null) {
				return npc;
			}
		}
		return new Combatant(generate(crmin, crmax), true);
	}

	Monster generate(int crmin, int crmax) {
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

	/**
	 * @return The result of {@link #select()} with a CR between
	 *         {@link Difficulty#DIFFICULT} and {@link Difficulty#DEADLY}.
	 */
	public Combatant select() {
		return select(Dungeon.active.level + Difficulty.DIFFICULT,
				Dungeon.active.level + Difficulty.DEADLY);
	}
}