package javelin.view.frame;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * Represents an AWT window.
 * 
 * @author alex
 */
public abstract class Frame {

	public JFrame frame;

	/**
	 * @param title
	 *            Window title.
	 */
	public Frame(String title) {
		frame = new JFrame(title);
	}

	/**
	 * @param frame
	 *            Positions this frame in relation to the screen.
	 * 
	 * @see #getscreensize()
	 */
	protected void position(JFrame frame) {
		Dimension screen = getscreensize();
		frame.setLocation(screen.width / 2 - frame.getSize().width / 2,
				screen.height / 2 - frame.getSize().height / 2);
	}

	/** Open dialog. */
	public void show() {
		frame.setContentPane(generate());
		frame.pack();
		position(frame);
		frame.setVisible(true);
	}

	/**
	 * @return the content pane.
	 */
	protected abstract Container generate();

	/**
	 * @return The screen size.
	 */
	public static Dimension getscreensize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
}