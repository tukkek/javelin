package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.model.unit.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;

/**
 * Allows a gladiator to be sold to the {@link Arena}.
 * 
 * @author alex
 */
public class SellGladiatorScreen extends Frame {
	static final HireGladiatorScreen HIRE = new HireGladiatorScreen();

	/** Constructor. */
	public SellGladiatorScreen() {
		super("Sell gladiator");
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		for (final Combatant c : ArenaWindow.arena.gladiators) {
			final int coins = HIRE.getcoins(c.source);
			parent.add(newbutton(c.toString() + " (" + coins + " coins)",
					parent, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (JOptionPane.showConfirmDialog(frame,
									"Are you sure you want to sell " + c + "?",
									"Confirmation",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								ArenaWindow.arena.gladiators.remove(c);
								ArenaWindow.arena.coins += coins;
								frame.dispose();
							}
						}
					})).setEnabled(ArenaWindow.arena.gladiators.size() > 1);
		}
		parent.add(new Label());
		newbutton("Return", parent, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		return parent;
	}
}
