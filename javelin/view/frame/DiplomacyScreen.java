package javelin.view.frame;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JLabel;

import javelin.model.diplomacy.Diplomacy;

/**
 * @see Diplomacy
 * @author alex
 */
public class DiplomacyScreen extends Frame{
	/** Constructor. */
	public DiplomacyScreen(){
		super("Diplomacy");
	}

	@Override
	protected Container generate(){
		var c=new Container();
		c.setLayout(new GridLayout(0,1));
		c.add(new JLabel("Óia diplomata viu!"));
		return c;
	}

	/** Opens this screen. */
	public static void open(){
		var w=new DiplomacyScreen();
		w.show();
		w.blockbackground();
	}
}
