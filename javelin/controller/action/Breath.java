package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.area.Area;
import javelin.controller.action.area.Burst;
import javelin.controller.action.area.Line;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.condition.Breathless;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.BreathOverlay;

/**
 * Use {@link BreathWeapon}s.
 * 
 * TODO use visible {@link Combatant}s as targets instead of predefined bursts
 * and lines.
 * 
 * @author alex
 */
public class Breath extends Action implements AiAction {
	/** Unique instance for this class. */
	public static final Action SINGLETON = new Breath();

	static final HashMap<Integer, Area> BURSTS = new HashMap<Integer, Area>();
	static final HashMap<Integer, Area> LINES = new HashMap<Integer, Area>();

	static {
		BURSTS.put(7, new Burst(-1, +1, -1, 0, -1, +1, 0, +1));
		BURSTS.put(8, new Burst(0, +1, -1, +1, 0, +1, +1, +1));
		BURSTS.put(9, new Burst(+1, +1, 0, +1, +1, 0, +1, -1));
		BURSTS.put(4, new Burst(-1, 0, -1, +1, -1, 0, -1, -1));
		BURSTS.put(6, new Burst(+1, 0, +1, +1, +1, 0, +1, -1));
		BURSTS.put(1, new Burst(-1, -1, -1, 0, -1, -1, 0, -1));
		BURSTS.put(2, new Burst(0, -1, -1, -1, 0, -1, +1, -1));
		BURSTS.put(3, new Burst(+1, -1, 0, -1, +1, -1, +1, 0));
		LINES.put(7, new Line(0 - 1, 0 + 1));
		LINES.put(8, new Line(0, 0 + 1));
		LINES.put(9, new Line(0 + 1, 0 + 1));
		LINES.put(4, new Line(0 - 1, 0));
		LINES.put(6, new Line(0 + 1, 0));
		LINES.put(1, new Line(0 - 1, 0 - 1));
		LINES.put(2, new Line(0, 0 - 1));
		LINES.put(3, new Line(0 + 1, 0 - 1));
	}

	private Breath() {
		super("Use breath weapon", new String[] { "b" });
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant combatant) {
		final ArrayList<List<ChanceNode>> chances =
				new ArrayList<List<ChanceNode>>();
		if (combatant.hascondition(Breathless.class) != null) {
			return chances;
		}
		for (final BreathWeapon breath : combatant.source.breaths) {
			for (final Area a : (breath.type == BreathArea.CONE ? BURSTS
					: LINES).values()) {
				Point source = a.initiate(combatant);
				if (source.x >= 0 && source.y >= 0
						&& source.x < gameState.map.length
						&& source.y < gameState.map[0].length) {
					final BattleState s2 = gameState.clone();
					chances.add(breath(breath, a, s2.clone(combatant), s2));
				}
			}
		}
		return chances;
	}

	@Override
	public boolean perform(Combatant hero) {
		if (hero.hascondition(Breathless.class) != null) {
			Game.message(hero + ": temporarily breathless...", Delay.WAIT);
			throw new RepeatTurn();
		}
		BreathWeapon breath = null;
		while (breath == null) {
			breath = selectbreath(hero.source);
		}
		clear();
		Area a = selectarea(hero, breath);
		BattleState state = Fight.state;
		Set<Point> area = a.fill(breath.range, hero, state);
		if (area.isEmpty()) {
			throw new RepeatTurn();
		}
		BreathOverlay overlay = new BreathOverlay(area);
		MapPanel.overlay = overlay;
		clear();
		Game.redraw();
		Game.message(
				targetinfo(state, area)
						+ "Press ENTER or b to confirm, q to quit.",
				Delay.NONE);
		char confirm = Game.getInput().getKeyChar();
		try {
			quit(confirm);
		} catch (RepeatTurn e) {
			overlay.clear();
			throw e;
		}
		if (confirm == '\n' || confirm == 'b') {
			state = state.clone();
			Action.outcome(breath(breath, a, state.clone(hero), state));
		}
		overlay.clear();
		return true;
	}

	Area selectarea(Combatant hero, BreathWeapon breath) {
		Game.message("Select a direction or press q to quit.", Delay.NONE);
		Area a = null;
		while (a == null) {
			a = (breath.type == BreathWeapon.BreathArea.CONE ? BURSTS : LINES)
					.get(selectdirection(hero));
		}
		return a;
	}

	String targetinfo(BattleState state, Set<Point> area) {
		String targets = "";
		for (Point p : area) {
			Combatant c = state.getcombatant(p.x, p.y);
			if (c != null) {
				targets += c + ", ";
			}
		}
		if (!targets.isEmpty()) {
			targets = "Targetting: "
					+ targets.substring(0, targets.length() - 2) + ".\n\n";
		}
		return targets;
	}

	static void clear() {
		Game.messagepanel.clear();
	}

	static ArrayList<ChanceNode> breath(final BreathWeapon breath, final Area a,
			final Combatant active, final BattleState s) {
		final ArrayList<ChanceNode> chances = new ArrayList<ChanceNode>();
		final ArrayList<Combatant> targets = new ArrayList<Combatant>();
		for (final Point p : a.fill(breath.range, active, s)) {
			final Combatant target = s.getcombatant(p.x, p.y);
			if (target != null) {
				targets.add(target);
			}
		}
		if (targets.isEmpty()) {
			return chances;
		}
		for (final Entry<Integer, Float> delay : definedelays(breath)
				.entrySet()) {
			final BattleState s2 = s.clone();
			final int breathless = delay.getKey();
			final Combatant active2 = s2.clone(active);
			if (breathless > 0) {
				active2.addcondition(
						new Breathless(active.ap + breathless, active2));
			}
			active2.ap += .5f;
			for (final Entry<Integer, Float> roll : Action
					.distributeRoll(breath.damage[0], breath.damage[1])
					.entrySet()) {
				final float damagechance = roll.getValue();
				final int damage = roll.getKey() + breath.damage[2];
				hit(targets, new ArrayList<Float>(),
						damagechance * delay.getValue(), damage, s2, breath,
						chances,
						active + " breaths " + breath.description + "!", "");
			}
		}
		return chances;
	}

	static TreeMap<Integer, Float> definedelays(final BreathWeapon breath) {
		final TreeMap<Integer, Float> delays;
		if (breath.delay) {
			delays = Action.distributeRoll(1, 4);
		} else {
			delays = new TreeMap<Integer, Float>();
			delays.put(0, 1f);
		}
		return delays;
	}

	static void hit(final ArrayList<Combatant> targets,
			final ArrayList<Float> chances, final float damagechance,
			final int damage, final BattleState s, final BreathWeapon breath,
			final ArrayList<ChanceNode> nodes, final String action,
			String affected) {
		final int i = chances.size();
		if (i == targets.size()) {
			nodes.add(new ChanceNode(s, sumchance(chances, damagechance),
					compound(action, affected), Delay.BLOCK));
			return;
		}
		final Combatant target = targets.get(i);
		Integer savedc = breath.save(target);
		final float savechance;
		if (savedc == null) {
			savechance = 0;
		} else {
			savedc = breath.savedc - savedc;
			savechance = savedc == null ? 0 : bind(savedc / 20f);
		}
		affected += target;
		if (savechance > 0) {
			damage(targets, chances, damagechance, damage, s, breath, nodes,
					action, affected + " resists,", target, savechance,
					breath.saveeffect);
		}
		damage(targets, chances, damagechance, damage, s, breath, nodes, action,
				affected, target, 1 - savechance, 1f);
	}

	static void damage(final ArrayList<Combatant> targets,
			final ArrayList<Float> chances, final float damagechance,
			final int damage, final BattleState s, final BreathWeapon breath,
			final ArrayList<ChanceNode> nodes, final String action,
			String affected, Combatant target, final float unsafechance,
			final float unsafeeffet) {
		final BattleState s2 = s.clone();
		target = s2.clone(target);
		final int damagetotarget = Math.round(damage * unsafeeffet);
		if (damagetotarget > 0) {
			target.damage(damagetotarget, s2, target.source.energyresistance);
			affected += " is " + target.getStatus() + ". ";
		}
		hit(targets, updatechances(chances, unsafechance), damagechance, damage,
				s2, breath, nodes, action, affected);
	}

	static String compound(String action, String affected) {
		return affected.isEmpty() ? action
				: action + "\n" + affected.substring(0, affected.length() - 1);
	}

	static ArrayList<Float> updatechances(final ArrayList<Float> chances,
			float x) {
		final ArrayList<Float> unsafechances =
				(ArrayList<Float>) chances.clone();
		unsafechances.add(x);
		return unsafechances;
	}

	static float sumchance(final ArrayList<Float> chances,
			final float damagechance) {
		float finalchance = damagechance;
		for (final float chance : chances) {
			finalchance = finalchance * chance;
		}
		return finalchance;
	}

	BreathWeapon selectbreath(Monster m) {
		int size = m.breaths.size();
		if (size == 0) {
			Game.message("Monster doesn't have breath attacks...", Delay.WAIT);
			throw new RepeatTurn();
		}
		if (size == 1) {
			return m.breaths.get(0);
		}
		int index = Javelin.choose("Select a breath attack or press q to quit",
				m.breaths, false, false);
		if (index >= 0) {
			return m.breaths.get(index);
		}
		throw new RepeatTurn();
	}

	/**
	 * Tyrant's screen Y axis is inverted :P
	 */
	public Integer selectdirection(Combatant hero) {
		KeyEvent direction = Game.getInput();
		int code = direction.getKeyCode();
		char key = direction.getKeyChar();
		quit(key);
		if (key == 'U' || key == '7') {
			return 1;
		}
		if (code == KeyEvent.VK_UP || key == 'I' || key == '8') {
			return 2;
		}
		if (key == 'O' || key == '9') {
			return 3;
		}
		if (code == KeyEvent.VK_LEFT || key == 'J' || key == '4') {
			return 4;
		}
		if (code == KeyEvent.VK_RIGHT || key == 'L' || key == '6') {
			return 6;
		}
		if (key == 'M' || key == '1') {
			return 7;
		}
		if (code == KeyEvent.VK_DOWN || key == ',' || key == '2') {
			return 8;
		}
		if (key == '.' || key == '3') {
			return 9;
		}
		quit('q');
		return null;
	}

	void quit(char key) {
		if (key == 'q') {
			throw new RepeatTurn();
		}
	}
}
