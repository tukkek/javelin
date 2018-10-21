package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.controller.action.SimpleAction;
import javelin.model.world.World;
import javelin.view.TextWindow;
import javelin.view.screen.WorldScreen;

/**
 * In-game help.
 *
 * @author alex
 */
public class Guide extends WorldAction implements SimpleAction{
	/** Guide. */
	public static final Guide HOWTO=new Guide(KeyEvent.VK_F1,
			World.scenario==null?"Minigames":World.scenario.helpfile,"F1");
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
	public static final Guide KITS=new Guide(KeyEvent.VK_F10,"Kits","F10");
	/** Guide. */
	public static final Guide DISCIPLINES=new Guide(KeyEvent.VK_F11,
			"Martial disciplines","F11");
	/** Guide. */
	public static final Guide QUESTIONS=new Guide(KeyEvent.VK_F12,"Questions",
			"F12");

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
		TextWindow window=TextWindow.open(name);
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
