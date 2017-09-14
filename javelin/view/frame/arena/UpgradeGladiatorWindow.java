package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.BoxLayout;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;
import tyrant.mikera.engine.RPG;

/**
 * Allows {@link Arena#gladiators} to be {@link Upgrade}d.
 * 
 * @author alex
 */
public class UpgradeGladiatorWindow extends Frame {
	static final int UPGRADESPERSESSION = 9;
	static final float XPPERCOIN = 1 / new Float(Arena.COINSPERCR);

	class UpgradeButton implements ActionListener {
		private Upgrade u;
		private float cost;

		public UpgradeButton(Upgrade u, float cost) {
			this.u = u;
			this.cost = cost;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			gladiator.xp = gladiator.xp.subtract(new BigDecimal(cost));
			u.upgrade(gladiator);
			upgrades.remove(u);
			fill();
			show();
		}

	}

	ActionListener buyxp = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			boost(gladiator, 1);
			show();
		}
	};
	ActionListener buymorexp = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			boost(gladiator, Arena.COINSPERCR);
			show();
		}
	};
	ActionListener redraw = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			upgrades.clear();
			fill();
			buyxp.actionPerformed(e);
		}
	};
	ActionListener doreturn = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	};

	ArrayList<Upgrade> upgrades = new ArrayList<Upgrade>(UPGRADESPERSESSION);
	Combatant gladiator;

	/**
	 * @param c
	 *            Unit to be upgraded.
	 */
	public UpgradeGladiatorWindow(Combatant c) {
		super("Upgrade " + c);
		this.gladiator = c;
		fill();
	}

	void fill() {
		ArrayList<Upgrade> all = new ArrayList<Upgrade>();
		for (Collection<Upgrade> upgrades : UpgradeHandler.singleton.getall()
				.values()) {
			all.addAll(upgrades);
		}
		while (upgrades.size() < UPGRADESPERSESSION) {
			Upgrade u = RPG.pick(all);
			if (upgrades.contains(u)
					|| !u.upgrade(gladiator.clone().clonesource())) {
				all.remove(u);
			} else {
				upgrades.add(u);
			}
		}
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

		parent.add(new Label("Current XP: " + gladiator.gethumanxp()));
		parent.add(new Label(
				"You currently have " + ArenaWindow.arena.coins + " coins."));
		int xppercoin = Math.round(100 * XPPERCOIN);
		newbutton("Buy more XP (1 coin = " + xppercoin + "XP)", parent, buyxp)
				.setEnabled(ArenaWindow.arena.coins >= 1);
		newbutton("Buy more XP (" + Arena.COINSPERCR + " coins = 100XP)",
				parent, buymorexp).setEnabled(
						ArenaWindow.arena.coins >= Arena.COINSPERCR);
		newbutton("Redraw upgrades (1 coin + " + xppercoin + "XP bonus)",
				parent, redraw).setEnabled(ArenaWindow.arena.coins >= 1);
		parent.add(new Label());

		for (Upgrade u : upgrades) {
			Combatant clone = gladiator.clone().clonesource();
			u.upgrade(clone);
			float cost = CrCalculator
					.calculaterawcr(clone.source)[1]
					- CrCalculator
							.calculaterawcr(gladiator.source)[1];
			if (cost < .1f) {
				cost = .1f;
			}
			newbutton(u + " (" + Math.round(100 * cost) + "XP)", parent,
					new UpgradeButton(u, cost))
							.setEnabled(gladiator.xp.floatValue() >= cost);
		}

		parent.add(new Label());
		newbutton("Return", parent, doreturn);
		return parent;
	}

	/**
	 * @param c
	 *            Given {@value #XPPERCOIN} CR in XP...
	 * @param multiplier
	 *            times this. Spends this number of {@link Arena#coins}.
	 */
	public static void boost(Combatant c, int multiplier) {
		c.learn(multiplier * XPPERCOIN);
		ArenaWindow.arena.coins -= multiplier;
	}

	@Override
	protected void enter() {
		// nothing
	}
}