package javelin.view.frame.arena;

import java.awt.Button;
import java.awt.Container;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;

/**
 * Allow {@link Item} buying in the {@link Arena}.
 * 
 * @author alex
 */
public class ArenaShoppingScreen extends Frame {
	class Buy implements ActionListener {
		Combatant c;

		public Buy(Combatant c) {
			this.c = c;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Item item = items.get(list.getSelectedIndex());
			ArenaWindow.arena.additem(item, c);
			ArenaWindow.arena.coins -= Arena.getcoins(item.price);
			show();
		}
	}

	class Selection implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			Item item = items.get(list.getSelectedIndex());
			for (int i = 0; i < roster.size(); i++) {
				gladiators.get(i)
						.setEnabled(item.canuse(roster.get(i)) == null);
			}
		}
	}

	List list = new List(20);
	ArrayList<Item> items = new ArrayList<Item>();
	ArrayList<Button> gladiators = new ArrayList<Button>();
	private ArrayList<Combatant> roster;

	/**
	 * @param rosterp
	 *            See {@link Arena#gladiators}.
	 */
	public ArenaShoppingScreen(ArrayList<Combatant> rosterp) {
		super("Buy item");
		roster = rosterp;
		frame.setMinimumSize(getdialogsize());
	}

	@Override
	protected Container generate() {
		items.clear();
		gladiators.clear();
		list.removeAll();
		for (int i = Item.ALL.size() - 1; i >= 0; i--) {
			Item item = Item.ALL.get(i);
			if (item instanceof Artifact || !item.usedinbattle) {
				continue;
			}
			int price = Arena.getcoins(item.price);
			if (ArenaWindow.arena.coins >= price) {
				items.add(item);
				list.add(item + " (" + price + " coins)");
			}
		}
		list.addItemListener(new Selection());
		Panel gladiators = new Panel();
		for (Combatant c : roster) {
			Panel gladiator = new Panel();
			gladiator.setLayout(new BoxLayout(gladiator, BoxLayout.Y_AXIS));
			Button b = newbutton(c.toString(), gladiator, new Buy(c));
			b.setEnabled(false);
			this.gladiators.add(b);
			for (Item i : ArenaWindow.arena.getitems(c)) {
				gladiator.add(new Label(i.toString()));
			}
			gladiators.add(gladiator);
		}
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		parent.add(list);
		parent.add(new Label("Who will buy this item? You have "
				+ ArenaWindow.arena.coins + " coins."));
		parent.add(gladiators);
		return parent;
	}

	@Override
	protected void enter() {
		// ignore
	}

	@Override
	public void show() {
		if (roster.isEmpty()) {
			JOptionPane.showMessageDialog(frame,
					"You need at least one gladiator selected to buy items!");
		} else {
			super.show();
		}
	}
}
