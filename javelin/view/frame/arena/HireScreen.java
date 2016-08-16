package javelin.view.frame.arena;

import java.awt.Button;
import java.awt.Container;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.unique.Arena;
import javelin.view.frame.Frame;

/**
 * Allows one to acquire {@link Arena#gladiators}.
 * 
 * @author alex
 */
public class HireScreen extends Frame {
	static final int MAXCR = Math.round(Javelin.MONSTERSBYCR.lastKey());

	List list = new List(20, false);
	JSlider slider;
	ArrayList<Monster> candidates = new ArrayList<Monster>();
	int costmultiplier = Arena.COINSPERCR;

	/** Constructor. */
	public HireScreen() {
		super("Hire a gladiator");
		frame.setMinimumSize(getdialogsize());
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		int max = Math.min(
				Math.round(Math.round(Math.floor(
						ArenaWindow.arena.coins / new Float(costmultiplier)))),
				MAXCR);
		slider = newslider(1, max, max);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});
		Label label = new Label("Which maximum level? You currently have "
				+ ArenaWindow.arena.coins + " coins.");
		parent.add(label);
		parent.add(slider);
		parent.add(list);
		update();
		Button hire = new Button("Hire");
		parent.add(hire);
		hire.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enter();
			}
		});
		return parent;
	}

	/**
	 * @return New slider with common configuration.
	 * @see JSlider#JSlider(int, int, int)
	 */
	static public JSlider newslider(int min, int max, int current) {
		JSlider slider = new JSlider(min, max, current);
		slider.setMajorTickSpacing(1);
		slider.setPaintLabels(true);
		return slider;
	}

	private void update() {
		int coins = slider.getValue();
		candidates.clear();
		for (Float cr : Javelin.MONSTERSBYCR.descendingKeySet()) {
			if (coins >= cr) {
				candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		}
		list.removeAll();
		for (Monster m : candidates) {
			list.add(m.toString() + " (" + getcoins(m) + " coins)");
		}
	}

	int getcoins(Monster m) {
		return costmultiplier
				* Math.round(Math.round(Math.ceil(m.challengeRating)));
	}

	@Override
	public void show() {
		if (ArenaWindow.arena.coins < costmultiplier) {
			JOptionPane.showMessageDialog(frame, "You need at least "
					+ costmultiplier + " coins to hire a new gladiator!");
			return;
		}
		super.show();
		frame.setPreferredSize(getdialogsize());
	}

	static final HireScreen open() {
		HireScreen s = new HireScreen();
		s.show();
		return s;
	}

	/**
	 * @param c
	 *            Selected unit.
	 */
	protected void select(Combatant c) {
		ArenaWindow.arena.gladiators.add(c);
	}

	@Override
	protected void enter() {
		int selection = list.getSelectedIndex();
		if (selection < 0) {
			return;
		}
		Monster gladiator = candidates.get(selection);
		ArenaWindow.arena.coins -= getcoins(gladiator);
		select(new Combatant(gladiator.clone(), true));
		frame.dispose();
	}
}
