package javelin.view.frame.arena;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.map.Map;
import javelin.model.unit.Combatant;
import javelin.view.screen.SquadScreen;
import tyrant.mikera.engine.RPG;

public class ArenaSetup extends javelin.view.frame.Frame {
	ActionListener dochangemap = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			drawmap();
			ArenaWindow.arena.coins -= 1;
			show();
		}
	};
	ActionListener dofight = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (map == null) {
				drawmap();
			}
			parent.action = new Runnable() {
				@Override
				public void run() {
					RandomEncounter fight = new RandomEncounter();
					fight.map = map;
					throw new StartBattle(fight);
				}
			};
			frame.dispose();
			parent.frame.dispose();
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
	Map map = null;
	ArrayList<Combatant> allies = new ArrayList<Combatant>();

	public ArenaSetup(ArenaWindow parent) {
		super("Arena battle setup");
		this.parent = parent;
		int nallies = RPG.r(3, 5) - ArenaWindow.arena.gladiators.size();
		while (nallies > 0) {
			Combatant ally = new Combatant(
					RPG.pick(SquadScreen.getcandidates()).clone(), true);
			ally.automatic = true;
			allies.add(ally);
			nallies -= 1;
		}
	}

	void drawmap() {
		ArrayList<Terrain> terrains =
				new ArrayList<Terrain>(Terrain.ALL.length);
		for (Terrain t : Terrain.ALL) {
			if (!Terrain.WATER.equals(t)) {
				terrains.add(t);
			}
		}
		terrains.add(Terrain.UNDERGROUND);
		Map map = null;
		while (map == null || (this.map != null && map.equals(this.map))) {
			map = RPG.pick(terrains).getmaps().pick();
		}
		this.map = map;
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
		newbutton("Start battle!", dofight, parent);
		newbutton("Return", doreturn, parent);
		return parent;
	}

	Component drawoptions() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		newbutton("Add temporary ally", doaddally, parent);
		newbutton("Buy item", null, parent);
		newbutton("Change map (1 coin)", dochangemap, parent)
				.setEnabled(ArenaWindow.arena.coins >= 1);
		parent.add(new Label("Current map: "
				+ (map == null ? "?" : map.name.toLowerCase())));
		return parent;
	}

	Component drawroster() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		parent.add(new Label("Who will participate in this fight?"));
		for (Combatant c : ArenaWindow.arena.gladiators) {
			parent.add(new Checkbox(c.toString(), true));
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
}