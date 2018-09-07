package javelin.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import javelin.view.frame.Frame;

public class TextWindow extends Frame{
	String text;

	public TextWindow(String title,String text){
		super(title);
		this.text=text;
	}

	@Override
	protected Container generate(){
		Container parent=new Panel();
		parent.setLayout(new BoxLayout(parent,BoxLayout.Y_AXIS));
		JTextArea text=new JTextArea(this.text);
		text.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		text.setEditable(false);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		Dimension size=getscreensize();
		size=new Dimension(size.width*3/4,size.height/2);
		JScrollPane scrollPane=new JScrollPane(text,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		parent.add(scrollPane).setPreferredSize(size);
		newbutton("Close",parent,e->frame.dispose());
		return parent;
	}
}