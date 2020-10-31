package javelin.model.world.location.dungeon.feature;

import java.awt.Image;
import java.util.Calendar;
import java.util.List;

import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.trap.Trap;
import javelin.view.Images;

/**
 * Purely cosmetic {@link Dungeon} items.
 *
 * TODO in the future, could hide hidden {@link Trap}, {@link Chest}, stc. The
 * easiest way to achieve this would simply be to make any appropriate Search
 * checks then replace this with the actual feature.
 *
 * @author alex
 */
public class Furniture extends Feature{
	static Image easteregg=null;

	static{
		var today=Calendar.getInstance();
		var d=today.get(Calendar.DAY_OF_MONTH);
		int m=today.get(Calendar.MONTH);
		if(d==31&&m==10)
			easteregg=Images.get(List.of("dungeon","furniture","halloween"));
		else if(d==25&&m==12)
			easteregg=Images.get(List.of("dungeon","furniture","christmas"));
	}

	/** Constructor. */
	public Furniture(String avatar){
		super(avatar,avatar);
	}

	@Override
	public boolean activate(){
		return false;
	}

	@Override
	public Image getimage(){
		if(easteregg!=null) return easteregg;
		return Images.get(List.of("dungeon","furniture",avatarfile));
	}
}
