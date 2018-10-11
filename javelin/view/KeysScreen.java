package javelin.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import javelin.controller.action.ActionDescription;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.view.frame.Frame;

/**
 * Configure world keys.
 *
 * @author alex
 */
public abstract class KeysScreen extends Frame{
	class RestoreDefault implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			String clean="";
			for(String s:Preferences.getfile().split("\n"))
				if(!s.startsWith(optionname)) clean+=s+"\n";
			Preferences.savefile(clean);
			StateManager.save(true,StateManager.SAVEFILE);
			System.exit(0);
		}
	}

	class ClearText implements MouseListener{
		private final TextField key;

		public ClearText(TextField key){
			this.key=key;
		}

		@Override
		public void mouseClicked(MouseEvent e){
			key.setText("");
		}

		@Override
		public void mouseReleased(MouseEvent e){}

		@Override
		public void mouseEntered(MouseEvent e){}

		@Override
		public void mouseExited(MouseEvent e){}

		@Override
		public void mousePressed(MouseEvent e){}
	}

	static final int COLUMNS=4;

	ArrayList<TextField> keys;
	String optionname;
	JScrollPane scrollPane;
	ArrayList<ActionDescription> actions;

	/** Constructor. */
	public KeysScreen(String title,String optionname){
		super(title);
		this.optionname=optionname;
	}

	@Override
	protected Container generate(){
		Panel content=new Panel();
		actions=getactions();
		content.setLayout(new GridLayout(0,COLUMNS));
		keys=new ArrayList<>(actions.size());
		int column=1;
		for(ActionDescription a:actions){
			content.add(new Label(a.getDescriptiveName()));
			final TextField key=new TextField(a.getMainKey(),1);
			content.add(key);
			keys.add(key);
			key.addMouseListener(new ClearText(key));
			column+=2;
			if(column>COLUMNS) column=1;
		}
		if(column!=1) while(column<=COLUMNS){
			content.add(new Label());
			column+=1;
		}
		content.add(new Label());
		content.add(new Label());
		java.awt.Button restore=new java.awt.Button(
				"Restore defaults (requires restart)");
		content.add(restore);
		restore.addActionListener(new RestoreDefault());
		java.awt.Button button=new java.awt.Button("Save");
		content.add(button);
		button.addActionListener(e->enter());
		scrollPane=new JScrollPane(content);
		return scrollPane;
	}

	/**
	 * @return All actions that can be configured by this screen.
	 */
	public abstract ArrayList<ActionDescription> getactions();

	@Override
	protected void enter(){
		String configure="";
		for(int i=0;i<actions.size();i++){
			TextField textarea=keys.get(i);
			String key=textarea.getText();
			if(configure.contains(key)||key.length()!=1){
				textarea.setBackground(Color.RED);
				return;
			}
			configure+=key;
			textarea.setBackground(null);
		}
		Preferences.setoption(optionname,configure);
		frame.dispose();
	}
}
