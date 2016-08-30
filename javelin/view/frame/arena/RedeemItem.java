package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;

/**
 * Allows a player to acquire {@link Item}s in exchange for {@link Arena#coins}.
 * 
 * @see Arena#items
 * @author alex
 */
public class RedeemItem extends Frame {
	class Buy implements ActionListener {
		Item i;

		public Buy(Item i) {
			this.i = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			arena.coins -= getcoins(i);
			arena.items.remove(i);
			arena.stock();
			parent.action = new Runnable() {
				@Override
				public void run() {
					i.grab();
				}
			};
			frame.dispose();
		}
	}

	Squad nearby = null;
	Arena arena = ArenaWindow.arena;
	ArenaWindow parent;

	/**
	 * Constructor.
	 * 
	 * @param arenaWindow
	 */
	public RedeemItem(ArenaWindow parent) {
		super("Redeem items");
		this.parent = parent;
		for (WorldActor s : Squad.getsquads()) {
			if (s.isadjacent(arena)) {
				nearby = (Squad) s;
				break;
			}
		}
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		if (nearby == null) {
			parent.add(new Label(
					"You need to visit the Arena with a party to redeem items."));
		}
		parent.add(
				new Label("You have " + arena.coins + " coins.", Label.CENTER));
		for (Item i : arena.items) {
			newbutton(i.toString() + " (" + getcoins(i) + " coins)", parent,
					new Buy(i)).setEnabled(
							nearby != null && arena.coins >= i.price);
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

	int getcoins(Item i) {
		return Arena.getcoins(i.price) * Arena.COINSPERCR;
	}
}
