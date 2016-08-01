package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.model.world.location.unique.Arena;
import javelin.view.frame.Frame;

public class ArenaWindow extends Frame {
	static final String ABOUT = "Welcome to the arena!\n\n" + //
			"Our services allow you to create a team and fight in battles and\n"
			+ "tournaments without having to travel the world around you! Here\n"
			+ "you will be able to grow in power faster and experience large,\n"
			+ "awesome battles if you put some effort early on!\n\n"
			+ "Many adventurers choose to ignore the complex world around them\n"
			+ "and get right into action by focusing on the arena! Others decide\n"
			+ "to use our services as a distraction from their larger quest...\n\n"
			+ "Most of the features of the arena, such as unlocking new\n"
			+ "gladiators, upgrading your current ones and acquiring items is done\n"
			+ "through Arena Coins, which you will receive by winning fights and\n"
			+ "betting on matches (even your own)! You will notice that once you\n"
			+ "start earning coins you will be able to achieve great things fast!\n\n"
			+ "Note that your arena gladiators and outside world party are not\n"
			+ "the same and shouldn't be confused - they are kept separate at all\n"
			+ "times so you can play in the arena without worrying about losing\n"
			+ "your party!";

	ActionListener dofight = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new ArenaSetup(ArenaWindow.this).show(ArenaWindow.this);
		}
	};
	private ActionListener doabout = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(frame, ABOUT, "About the arena",
					JOptionPane.PLAIN_MESSAGE);
		}
	};
	private ActionListener dohire = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			block(HireScreen.open());
		}
	};
	private ActionListener doview = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new ViewGladiators(arena).show(ArenaWindow.this);
		}
	};

	public static Arena arena;
	public Runnable action;

	public ArenaWindow(Arena arenap) {
		super("Arena");
		arena = arenap;
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		newbutton("Fight!", dofight, parent);
		parent.add(new Label());
		newbutton("View gladiators", doview, parent);
		newbutton("Hire gladiator", dohire, parent);
		parent.add(new Label());
		newbutton("About the Arena", doabout, parent);
		parent.add(new Label("You currently have " + arena.coins + " coins."));
		return parent;
	}

	@Override
	public void show() {
		if (arena.welcome) {
			doabout.actionPerformed(null);
			HireScreen.open().defer();
			if (arena.gladiators.isEmpty()) {
				return;
			}
			arena.welcome = false;
		}
		super.show();
	}
}