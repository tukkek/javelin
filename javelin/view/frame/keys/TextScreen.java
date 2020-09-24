package javelin.view.frame.keys;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import javelin.view.frame.Frame;

/**
 * Opens a window which mostly consists of a {@link TextArea}.
 *
 * @author alex
 */
public abstract class TextScreen extends Frame{
	final TextArea text=new TextArea(loadtext(),30,80,
			TextArea.SCROLLBARS_VERTICAL_ONLY);

	/** Constructor. */
	public TextScreen(String title){
		super(title);
	}

	/**
	 * @param string Saves the given text.
	 */
	protected abstract void savetext(final String string);

	/**
	 * @return Loads the {@link TextArea} content.
	 */
	protected abstract String loadtext();

	@Override
	protected Container generate(){
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(text,BorderLayout.CENTER);
		var save=new Button();
		save.setLabel("Save");
		save.addActionListener(e->enter());
		panel.add(save,BorderLayout.SOUTH);
		return panel;
	}

	@Override
	public void show(){
		text.addKeyListener(new KeyAdapter(){
			@Override
			public void keyReleased(KeyEvent e){
				super.keyReleased(e);
				var key=e.getKeyCode();
				if(key==KeyEvent.VK_ESCAPE)
					frame.dispose();
				else if(key==KeyEvent.VK_ENTER&&e.isControlDown()) enter();
			}
		});
		super.show();
		text.requestFocus();
	}

	@Override
	protected void enter(){
		savetext(text.getText());
		frame.dispose();
	}
}