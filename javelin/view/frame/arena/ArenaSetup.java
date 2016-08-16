package javelin.view.frame.arena;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.ArenaFight;
import javelin.model.unit.Combatant;
import javelin.model.world.location.unique.Arena;
import javelin.view.screen.SquadScreen;
import tyrant.mikera.engine.RPG;

/**
 * Configures parameters for a {@link ArenaFight} in exchange for
 * {@link Arena#coins}.
 * 
 * @author alex
 */
public class ArenaSetup extends javelin.view.frame.Frame {
	ActionListener dochangemap = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fight.drawmap();
			ArenaWindow.arena.coins -= 1;
			show();
		}
	};
	ActionListener dochangeperiod = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fight.drawperiod();
			ArenaWindow.arena.coins -= 1;
			show();
		}
	};
	ActionListener dochangeweather = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fight.drawweather();
			ArenaWindow.arena.coins -= 1;
			show();
		}
	};
	ActionListener doaddmeld = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fight.nmeld += 1;
			ArenaWindow.arena.coins -= 1;
			show();
		}
	};
	ActionListener dobet = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (getroster().isEmpty() && allies.isEmpty()) {
				JOptionPane.showMessageDialog(frame,
						"Select some gladiators first!");
				return;
			}
			fight.addgladiators(getroster());
			fight.addgladiators(allies);
			fight.generate();
			frame.setContentPane(drawbetpanel());
			frame.setLocationRelativeTo(null);
			frame.pack();
		}
	};
	ActionListener dofight = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			parent.action = new Runnable() {
				@Override
				public void run() {
					throw new StartBattle(fight);
				}
			};
			frame.dispose();
		}
	};
	ActionListener doreturn = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to go back and lose any coins spent so far?",
					"Warning!",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				frame.dispose();
			}
		}
	};
	ActionListener doaddally = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new AllyScreen(ArenaSetup.this).show(ArenaSetup.this);
		}
	};

	ArenaWindow parent;
	ArrayList<Combatant> allies = new ArrayList<Combatant>();
	ArrayList<Checkbox> roster =
			new ArrayList<Checkbox>(ArenaWindow.arena.gladiators.size());
	final ArenaFight fight;

	/** Constructor. */
	public ArenaSetup(ArenaWindow parent, ArenaFight f) {
		super("Arena battle setup");
		this.parent = parent;
		fight = f;
		int nallies = 3 - ArenaWindow.arena.gladiators.size();
		while (nallies > 0) {
			Combatant ally = new Combatant(
					RPG.pick(SquadScreen.getcandidates()).clone(), true);
			ally.automatic = true;
			allies.add(ally);
			nallies -= 1;
		}
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BorderLayout(10, 10));
		parent.add(drawinfo(), BorderLayout.NORTH);
		parent.add(drawroster(), BorderLayout.WEST);
		parent.add(drawoptions(), BorderLayout.EAST);
		parent.add(drawactions(), BorderLayout.SOUTH);
		return parent;
	}

	private Component drawinfo() {
		Panel parent = new Panel();
		parent.add(new Label(
				"You currently have " + ArenaWindow.arena.coins + " coins."));
		return parent;
	}

	Component drawactions() {
		Panel parent = new Panel();
		newbutton("Bet", parent, dobet);
		newbutton("Return", parent, doreturn);
		return parent;
	}

	Component drawoptions() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		newbutton("Add temporary ally", parent, doaddally);
		boolean hascoins = ArenaWindow.arena.coins >= 1;
		newbutton("Add meld (1 coin)", parent, doaddmeld).setEnabled(hascoins);
		newbutton("Change map (1 coin)", parent, dochangemap)
				.setEnabled(hascoins);
		newbutton("Change period (1 coin)", parent, dochangeperiod)
				.setEnabled(hascoins);
		newbutton("Change weather (1 coin)", parent, dochangeweather)
				.setEnabled(hascoins);
		parent.add(new Label());
		parent.add(new Label("Meld count: " + fight.nmeld, Label.CENTER));
		parent.add(new Label("Map: "
				+ (fight.map == null ? "?" : fight.map.name.toLowerCase()),
				Label.CENTER));
		parent.add(new Label("Period: "
				+ (fight.period == null ? "?" : fight.period.toLowerCase()),
				Label.CENTER));
		parent.add(new Label("Weather: " + describeflood(fight.weather),
				Label.CENTER));
		return parent;
	}

	private String describeflood(Integer weather) {
		if (weather == null) {
			return "?";
		}
		if (weather == Weather.DRY) {
			return "dry";
		}
		if (weather == Weather.RAIN) {
			return "rain";
		}
		if (weather == Weather.STORM) {
			return "storm";
		}
		throw new RuntimeException("Unknown weather #arenasetup");
	}

	Component drawroster() {
		roster.clear();
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		parent.add(new Label("Who will participate in this fight?"));
		for (Combatant c : ArenaWindow.arena.gladiators) {
			Checkbox checkbox = new Checkbox(c.toString(), true);
			parent.add(checkbox);
			roster.add(checkbox);
		}
		if (!allies.isEmpty()) {
			parent.add(new Label());
			parent.add(new Label("Temporary allies:"));
			for (Combatant c : allies) {
				Checkbox checkbox = new Checkbox(c.toString(), true);
				checkbox.setEnabled(false);
				parent.add(checkbox);
			}
		}
		return parent;
	}

	@Override
	protected void escape() {
		doreturn.actionPerformed(null);
	}

	ArrayList<Combatant> getroster() {
		ArrayList<Combatant> roster = new ArrayList<Combatant>();
		for (int i = 0; i < this.roster.size(); i++) {
			if (this.roster.get(i).getState()) {
				roster.add(ArenaWindow.arena.gladiators.get(i));
			}
		}
		return roster;
	}

	Container drawbetpanel() {
		int el = ChallengeRatingCalculator.calculateel(fight.redteam);
		final JSlider slider = HireScreen.newslider(0,
				Math.min(ArenaWindow.arena.coins, Math.max(1, el / 2)), 0);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fight.bet = slider.getValue();
			}
		});
		Panel teams = new Panel(new GridLayout(0, 2));
		teams.add(new Label("Your team:", Label.CENTER));
		teams.add(new Label("Opponent team:", Label.CENTER));
		teams.add(new Label());
		teams.add(new Label());
		for (int i = 0; i < Math.max(fight.blueteam.size(),
				fight.redteam.size()); i++) {
			teams.add(new Label(i < fight.blueteam.size()
					? fight.blueteam.get(i).toString() : ""));
			teams.add(new Label(i < fight.redteam.size()
					? fight.redteam.get(i).toString() : ""));
		}
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		parent.add(new Label("How many coins will you bet?", Label.CENTER));
		parent.add(slider);
		parent.add(new Label());
		parent.add(teams);
		parent.add(new Label());
		parent.add(newbutton("Start fight!", null, dofight));
		return parent;
	}
}