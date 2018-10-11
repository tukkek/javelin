package javelin.controller.quality.subtype;

import javelin.controller.quality.Quality;
import javelin.model.unit.Monster;

/**
 * Similar to {@link Undead}.
 *
 * @author alex
 */
public class Construct extends Quality{
	public Construct(){
		super("construct");
	}

	@Override
	public void add(String declaration,Monster m){
		m.vision=Math.max(m.vision,Monster.VISION_LOWLIGHT);
		m.heal=false;
	}

	@Override
	public boolean has(Monster m){
		return m.type.equals("construct");
	}

	@Override
	public float rate(Monster m){
		/* see respective HD and Quality factors */
		return 0;
	}

	@Override
	public boolean apply(String attack,Monster m){
		return super.apply(attack,m)||has(m);
	}
}
