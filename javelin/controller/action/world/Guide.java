package javelin.controller.action.world;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javelin.controller.TextReader;
import javelin.controller.action.SimpleAction;
import javelin.view.frame.Frame;
import javelin.view.screen.WorldScreen;

/**
 * In-game help.
 * 
 * @author alex
 */
public class Guide extends WorldAction implements SimpleAction {
	class GuideWindow extends Frame {
		public GuideWindow() {
			super(name);
		}

		@Override
		protected Container generate() {
			Container parent = new Panel();
			parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
			JTextArea text = new JTextArea(TextReader.read(new File("doc",
					name.replaceAll(" ", "").toLowerCase() + ".txt")));
			text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			text.setEditable(false);
			text.setWrapStyleWord(true);
			text.setLineWrap(true);
			Dimension size = getscreensize();
			parent.add(new JScrollPane(text,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED))
					.setPreferredSize(
							new Dimension(size.width * 3 / 4, size.height / 2));
			newbutton("Close", parent, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
				}
			});

			return parent;
		}
	}

	/** Constructor. */
	public Guide(int vk, String name, String descriptive) {
		super(name, new int[] { vk }, new String[] { descriptive });
	}

	@Override
	public void perform(WorldScreen screen) {
		perform();
	}

	@Override
	public void perform() {
		GuideWindow window = new GuideWindow();
		window.show();
		window.defer();
		// TextReader.show(, "");

		// Javelin.app.switchScreen(BattleScreen.active);
	}

	@Override
	public int[] getcodes() {
		return keys;
	}

	@Override
	public String getname() {
		return name;
	}

	@Override
	public String[] getkeys() {
		return morekeys;
	}
}
