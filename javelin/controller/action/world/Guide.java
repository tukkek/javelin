package javelin.controller.action.world;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import javelin.JavelinApp;
import javelin.controller.TextReader;
import javelin.controller.action.SimpleAction;
import javelin.model.world.World;
import javelin.view.frame.Frame;
import javelin.view.screen.WorldScreen;

/**
 * In-game help.
 *
 * @author alex
 */
public class Guide extends WorldAction implements SimpleAction{
	/** Guide. */
	public static final Guide HOWTO=new Guide(KeyEvent.VK_F1,
			JavelinApp.minigame==null?World.scenario.helpfile:"Minigames","F1");
	/** Guide. */
	public static final Guide MINIGAMES=new Guide(KeyEvent.VK_F2,"Minigames",
			"F2");
	/** Guide. */
	public static final Guide ARTIFACTS=new Guide(KeyEvent.VK_F3,"Artifacts",
			"F3");
	/** Guide. */
	public static final Guide CONDITIONS=new Guide(KeyEvent.VK_F4,"Conditions",
			"F4");
	/** Guide. */
	public static final Guide ITEMS=new Guide(KeyEvent.VK_F5,"Items","F5");
	/** Guide. */
	public static final Guide SKILLS=new Guide(KeyEvent.VK_F6,"Skills","F6");
	/** Guide. */
	public static final Guide SPELLS=new Guide(KeyEvent.VK_F7,"Spells","F7");
	/** Guide. */
	public static final Guide UGRADES=new Guide(KeyEvent.VK_F8,"Upgrades","F8");
	/** Guide. */
	public static final Guide DISTRICT=new Guide(KeyEvent.VK_F9,"District","F9");
	/** Guide. */
	public static final Guide KITS=new Guide(KeyEvent.VK_F10,"Kits","F10");;
	/** Guide. */
	public static final Guide DISCIPLINES=new Guide(KeyEvent.VK_F11,
			"Martial disciplines","F11");

	class GuideWindow extends Frame{
		public GuideWindow(){
			super(name+" guide");
		}

		@Override
		protected Container generate(){
			Container parent=new Panel();
			parent.setLayout(new BoxLayout(parent,BoxLayout.Y_AXIS));
			JTextArea text=new JTextArea(TextReader
					.read(new File("doc",name.replaceAll(" ","").toLowerCase()+".txt")));
			text.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			text.setEditable(false);
			text.setWrapStyleWord(true);
			text.setLineWrap(true);
			Dimension size=getscreensize();
			parent
					.add(new JScrollPane(text,
							ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED))
					.setPreferredSize(new Dimension(size.width*3/4,size.height/2));
			newbutton("Close",parent,e->frame.dispose());

			return parent;
		}
	}

	/** Constructor. */
	public Guide(int vk,String name,String descriptive){
		super(name,new int[]{vk},new String[]{descriptive});
	}

	@Override
	public void perform(WorldScreen screen){
		perform();
	}

	@Override
	public void perform(){
		GuideWindow window=new GuideWindow();
		window.show();
		window.defer();
	}

	@Override
	public int[] getcodes(){
		return keys;
	}

	@Override
	public String getname(){
		return name;
	}

	@Override
	public String[] getkeys(){
		return morekeys;
	}
}
