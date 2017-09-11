package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.area.Area;
import javelin.controller.action.area.Burst;
import javelin.controller.action.area.Line;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.StopThinking;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Breathless;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.mappanel.battle.overlay.BreathOverlay;

/**
 * Use {@link BreathWeapon}s.
 * 
 * TODO use visible {@link Combatant}s as targets instead of predefined bursts
 * and lines.
 * 
 * This class was causing problems so I streamlined it:
 * 
 * TODO removed d4 for delay and made it constatnt to streamline/quicken
 * calculation
 * 
 * TODO currently decides if saved if save chance >=50% for the same reasons.
 * 
 * @author alex
 */
public class Breath extends Action implements AiAction {
	static public class BreathNode extends ChanceNode {
		public BreathNode(Node n, float chance, String action, Collection<Point> area) {
			super(n, chance, action, Delay.BLOCK);
			overlay = new AiOverlay(area);
		}
	}

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
	public List<List<ChanceNode>> getoutcomes(final Combatant combatant, final BattleState s) {
		final ArrayList<List<ChanceNode>> chances = new ArrayList<List<ChanceNode>>();
		if (combatant.hascondition(Breathless.class) != null) {
			return chances;
		}
		for (final BreathWeapon breath : combatant.source.breaths) {
			for (final Area a : (breath.type == BreathArea.CONE ? BURSTS : LINES).values()) {
				Point source = a.initiate(combatant);
				if (source.x >= 0 && source.y >= 0 && source.x < s.map.length && source.y < s.map[0].length) {
					chances.add(breath(breath, a, combatant, s));
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
		Game.message(targetinfo(state, area) + "Press ENTER or b to confirm, q to quit.", Delay.NONE);
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
			a = (breath.type == BreathWeapon.BreathArea.CONE ? BURSTS : LINES).get(selectdirection(hero));
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
			targets = "Targetting: " + targets.substring(0, targets.length() - 2) + ".\n\n";
		}
		return targets;
	}

	static void clear() {
		Game.messagepanel.clear();
	}

	static ArrayList<ChanceNode> breath(final BreathWeapon breath, final Area a, Combatant active, BattleState s) {
		final ArrayList<ChanceNode> chances = new ArrayList<ChanceNode>();
		final ArrayList<Combatant> targets = new ArrayList<Combatant>();
		final Set<Point> area = a.fill(breath.range, active, s);
		for (final Point p : area) {
			final Combatant target = s.getcombatant(p.x, p.y);
			if (target != null) {
				targets.add(target);
			}
		}
		if (targets.isEmpty()) {
			return chances;
		}
		s = s.clone();
		active = s.clone(active);
		if (breath.delay) {
			active.addcondition(new Breathless(active.ap + (1 + 4) / 2f, active));
		}
		active.ap += .5f;
		for (final Entry<Integer, Float> roll : Action.distributeroll(breath.damage[0], breath.damage[1]).entrySet()) {
			BattleState s2 = s.clone();
			final float damagechance = roll.getValue();
			final int damage = roll.getKey() + breath.damage[2];
			StringBuilder action = new StringBuilder(active + " breaths " + breath.description + "!");
			StringBuilder affected = new StringBuilder();
			for (Combatant target : targets) {
				if (Thread.interrupted()) {
					throw new StopThinking();
				}
				hit(s2.clone(target), damage, s2, breath, affected);
			}
			ChanceNode n = new BreathNode(s2, damagechance, compound(action.toString(), affected.toString()), area);
			chances.add(n);
		}
		return chances;
	}

	static void hit(Combatant target, final int damage, final BattleState s, final BreathWeapon breath,
			StringBuilder affected) {
		if (target == null) {
			System.out.println("#breath null target");
		}
		Integer savedc = breath.save(target);
		final float savechance;
		if (savedc == null) {
			savechance = 0;
		} else {
			savedc = breath.savedc - savedc;
			savechance = savedc == null ? 0 : bind(savedc / 20f);
		}
		affected.append(target);
		if (savechance >= 0.5f) {
			affected.append(" resists,");
			damage(target, Math.round(damage * breath.saveeffect), s, affected);
		} else {
			damage(target, damage, s, affected);
		}
	}

	static void damage(Combatant target, final int damage, final BattleState s, StringBuilder affected) {
		if (damage > 0) {
			target.damage(damage, s, target.source.energyresistance);
			affected.append(" is " + target.getstatus() + ". ");
		}
	}

	static String compound(String action, String affected) {
		return affected.isEmpty() ? action : action + "\n" + affected.substring(0, affected.length() - 1);
	}

	// static ArrayList<Float> updatechances(final ArrayList<Float> chances,
	// float x) {
	// final ArrayList<Float> unsafechances = (ArrayList<Float>)
	// chances.clone();
	// unsafechances.add(x);
	// return unsafechances;
	// }

	// static float sumchance(final ArrayList<Float> chances, final float
	// damagechance) {
	// float finalchance = damagechance;
	// for (final float chance : chances) {
	// finalchance = finalchance * chance;
	// }
	// return finalchance;
	// }

	BreathWeapon selectbreath(Monster m) {
		int size = m.breaths.size();
		if (size == 0) {
			Game.message("Monster doesn't have breath attacks...", Delay.WAIT);
			throw new RepeatTurn();
		}
		if (size == 1) {
			return m.breaths.get(0);
		}
		int index = Javelin.choose("Select a breath attack or press q to quit", m.breaths, false, false);
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
