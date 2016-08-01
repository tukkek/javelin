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

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.view.frame.Frame;
import tyrant.mikera.engine.RPG;

public class UpgradeWindow extends Frame {
	private static final int UPGRADESPERSESSION = 9;

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
			boost(gladiator);
			show();
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

	public UpgradeWindow(Combatant c) {
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
		parent.add(new Label("Coins: " + ArenaWindow.arena.coins));
		newbutton("Buy more XP (1 coin = 10XP)", buyxp, parent)
				.setEnabled(ArenaWindow.arena.coins >= 1);
		parent.add(new Label());

		for (Upgrade u : upgrades) {
			Combatant clone = gladiator.clone().clonesource();
			u.upgrade(clone);
			float cost =
					ChallengeRatingCalculator.calculaterawcr(clone.source)[1]
							- ChallengeRatingCalculator
									.calculaterawcr(gladiator.source)[1];
			if (cost < .1f) {
				cost = .1f;
			}
			newbutton(u + " (" + Math.round(100 * cost) + "XP)",
					new UpgradeButton(u, cost), parent)
							.setEnabled(gladiator.xp.floatValue() >= cost);
		}

		parent.add(new Label());
		newbutton("Return", doreturn, parent);
		return parent;
	}

	public static void boost(Combatant c) {
		c.xp = c.xp.add(new BigDecimal(.1f));
		ArenaWindow.arena.coins -= 1;
	}
}