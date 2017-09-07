package javelin.view.frame.arena;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;

import javax.swing.BoxLayout;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;

/**
 * Select a unit and open a {@link ViewGladiator} scren.
 * 
 * @author alex
 */
public class ViewGladiators extends Frame {
	/** Constructor. */
	public ViewGladiators(Arena arena) {
		super("Select a gladiator");
		frame.setMinimumSize(new Dimension(getdialogsize().width / 2, 0));
	}

	@Override
	protected Container generate() {
		Panel panel = new Panel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ArenaWindow.arena.gladiators.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		for (final Combatant c : ArenaWindow.arena.gladiators) {
			ChallengeRatingCalculator.calculatecr(c.source);
			Button b = new Button(c.toString() + " (level "
					+ Math.round(c.source.challengerating) + ")");
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new ViewGladiator(c, ArenaWindow.arena)
							.show(ViewGladiators.this);
				}
			});
			panel.add(b);
		}
		panel.add(new Label());
		Button b = new Button("Return");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		panel.add(b);
		return panel;
	}
}