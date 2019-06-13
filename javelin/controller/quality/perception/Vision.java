package javelin.controller.quality.perception;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see Monster#vision
 *
 * @author alex
 */
public class Vision extends Quality{
	/** Provides {@link Monster#VISION_LOWLIGHT}. */
	public static final Upgrade LOWLIGHTVISION=new VisionUpgrade(
			"Low-light vision",1);
	/** Provides {@link Monster#VISION_DARK}. */
	public static final Upgrade DARKVISION=new VisionUpgrade("Darkvision",2);

	/**
	 * See the d20 SRD for more info.
	 */
	static class VisionUpgrade extends Upgrade{
		int target;

		VisionUpgrade(String name,int targetp){
			super(name);
			target=targetp;
		}

		@Override
		public String inform(Combatant m){
			switch(m.source.vision){
				case 0:
					return "Currently: mormal vision";
				case 1:
					return "Currently: low-light vision";
				case 2:
					return "Currently: darkvision";
			}
			throw new RuntimeException("Unknown vision");
		}

		@Override
		public boolean apply(Combatant m){
			if(m.source.vision>=target) return false;
			m.source.vision=target;
			return true;
		}
	}

	final int target;

	public Vision(String name,int target){
		super(name);
		this.target=target;
	}

	@Override
	public void add(final String declaration,final Monster m){
		if(target>m.vision) m.vision=target;
	}

	@Override
	public boolean has(Monster monster){
		return monster.vision==target;
	}

	@Override
	public float rate(Monster monster){
		if(monster.vision==2) return .2f;
		if(monster.vision==1) return .1f;
		return 0;
	}

	@Override
	public String describe(Monster m){
		if(m.vision==1) return "low-light vision";
		if(m.vision==2) return "darkvision";
		return null;
	}
}