package javelin.view.screen;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.old.QuestApp;
import javelin.old.Screen;
import javelin.old.messagepanel.Text;

/**
 * Fullscreen text display. For hiding the current {@link BattleScreen} or
 * {@link WorldScreen} and interacting textually with the user.
 *
 * @author alex
 */
public class InfoScreen extends Screen {
	public static final char ESCAPE = '\u001B';

	/** Screen content. */
	public String text;
	/** {@link #text} rendering style.t */
	public Font font;

	final int border = 20;

	/** Constructor. */
	public InfoScreen(final String textp) {
		text = textp;
		font = QuestApp.mainfont;
		setForeground(QuestApp.INFOTEXTCOLOUR);
		setBackground(QuestApp.INFOSCREENCOLOUR);
		setFont(font);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final FontMetrics met = g.getFontMetrics(g.getFont());
		final Rectangle r = getBounds();
		final int charsize = met.charWidth(' ');
		final int linelength = (r.width - border * 2) / charsize;
		int y = border + met.getMaxAscent();
		final int lineinc = met.getMaxAscent() + met.getMaxDescent();
		for (final String s : Text.separateString(text, '\n')) {
			for (final String line : Text.wrapString(s, linelength)) {
				g.setColor(QuestApp.INFOTEXTCOLOUR);
				g.drawString(line, border, y);
				y += lineinc;
			}
		}
	}

	/**
	 * @return A non-negative integer.
	 */
	static public int numberfeedback() {
		while (true) {
			try {
				return Integer.parseInt(feedback().toString());
			} catch (final NumberFormatException e2) {
				continue;
			}
		}
	}

	/**
	 * @return Player input.
	 * @see #ESCAPE
	 */
	static public Character feedback() {
		KeyEvent input = Javelin.input();
		switch (input.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			return '→';
		case KeyEvent.VK_LEFT:
			return '←';
		case KeyEvent.VK_BACK_SPACE:
			return '\b';
		case KeyEvent.VK_SHIFT:
			return feedback();
		case KeyEvent.VK_ESCAPE:
			return ESCAPE;
		case KeyEvent.VK_TAB:
			return '\t';
		}
		return Character.valueOf(input.getKeyChar());
	}

	/**
	 * @param string
	 *            Replace {@link #text} and update screen.
	 */
	public void print(String string) {
		text = string;
		Javelin.app.switchScreen(this);
	}

	/**
	 * @param msg
	 *            Prints this meesage, which will go away after a keyboard input
	 *            is received.
	 * @return The key pressed by the player to acknowledge the message and make
	 *         it disappear.
	 */
	protected Character printmessage(String msg) {
		String current = text;
		print(current + "\n" + msg);
		Character c = feedback();
		text = current;
		return c;
	}
}