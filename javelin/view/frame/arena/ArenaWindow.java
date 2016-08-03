package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.controller.fight.ArenaFight;
import javelin.model.world.location.unique.Arena;
import javelin.view.frame.Frame;

/**
 * Main view for {@link Arena}.
 * 
 * @author alex
 */
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
			+ "start earning a few coins you will be able to achieve great things fast!\n\n"
			+ "Note that your arena gladiators and outside world party are not\n"
			+ "the same and shouldn't be confused - they are kept separate at all\n"
			+ "times so you can play in the arena without worrying about losing\n"
			+ "your main units.\n\n"
			+ "Note that you don't have to actually visit the Arena to activate\n"
			+ "it! You can open this screen from anywhere in the game world and\n"
			+ "anytime - just check the help menu or key configuration screen to\n"
			+ "find out what the proper key to do so is.";

	ActionListener dofight = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new ArenaSetup(ArenaWindow.this, new ArenaFight())
					.show(ArenaWindow.this);
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
	ActionListener dobuy = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new BuyScreen(arena.gladiators).show(ArenaWindow.this);
		}
	};

	/** Helper field, updated on {@link #show()}. */
	public static Arena arena;
	/**
	 * If not <code>null</code> will close this {@link Frame} and run this in
	 * the main game thread.
	 */
	public Runnable action;

	/** Constructor. */
	public ArenaWindow(Arena arenap) {
		super("Arena");
		arena = arenap;
	}

	@Override
	protected Container generate() {
		Panel parent = new Panel();
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
		newbutton("Fight!", parent, dofight);
		parent.add(new Label());
		newbutton("Buy item", parent, dobuy);
		newbutton("View gladiators", parent, doview);
		newbutton("Hire gladiator", parent, dohire);
		parent.add(new Label());
		newbutton("About the Arena", parent, doabout);
		parent.add(new Label("You currently have " + arena.coins + " coins."));
		return parent;
	}

	@Override
	public void show() {
		if (action != null) {
			frame.dispose();
			return;
		}
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

	@Override
	protected void enter() {
		dofight.actionPerformed(null);
	}
}