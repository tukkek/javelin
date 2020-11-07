package javelin.model.world.location.dungeon.feature;

import java.awt.Image;
import java.util.Calendar;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.view.Images;

/**
 * Mostly cosmetic {@link DungeonFloor} items. Can contain {@link #hidden} features.
 *
 * TODO in the future, could hide hidden {@link Trap}, {@link Chest}, stc. The
 * easiest way to achieve this would simply be to make any appropriate Search
 * checks then replace this with the actual feature.
 *
 * @see Trap
 * @author alex
 */
public class Furniture extends Feature{
	static final String FOUND="You have found a hidden %s!";

	static Image easteregg=null;
	/** Custom {@link #hidden} reveal message. */
	public static String revealmessage;
	/** Callback for {@link #hidden} features. */
	public static Runnable onreveal;

	static{
		var today=Calendar.getInstance();
		var d=today.get(Calendar.DAY_OF_MONTH);
		int m=today.get(Calendar.MONTH);
		if(d==31&&m==10)
			easteregg=Images.get(List.of("dungeon","furniture","halloween"));
		else if(d==25&&m==12)
			easteregg=Images.get(List.of("dungeon","furniture","christmas"));
	}

	Feature hidden;

	/** Constructor. */
	public Furniture(String avatar){
		super(avatar);
	}

	@Override
	public boolean activate(){
		if(hidden==null) return false;
		var c=Squad.active.getbest(Skill.PERCEPTION);
		if(!hidden.reveal(hidden.discover(c,c.taketen(Skill.PERCEPTION))))
			return false;
		remove();
		hidden.place(Dungeon.active,getlocation());
		Javelin.redraw();
		var message=revealmessage;
		if(message==null)
			message=String.format(FOUND,hidden.description.toLowerCase());
		else
			revealmessage=null;
		Javelin.message(message,true);
		WorldMove.abort=true;
		if(onreveal!=null) onreveal.run();
		onreveal=null;
		return true;
	}

	@Override
	public Image getimage(){
		if(easteregg!=null) return easteregg;
		return Images.get(List.of("dungeon","furniture",avatarfile));
	}

	public void hide(Feature f){
		hidden=f;
	}
}
