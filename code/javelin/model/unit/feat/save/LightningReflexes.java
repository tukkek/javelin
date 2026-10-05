package javelin.model.unit.feat.save;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class LightningReflexes extends SaveFeat{
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON=new LightningReflexes();

	private LightningReflexes(){
		super("lightning reflexes");
	}

	@Override
	public Integer getbonus(Monster m){
		return m.ref;
	}

	@Override
	public void setbonus(Monster m,int value){
		m.ref=value;
	}

	@Override
	protected String getname(){
		return "reflex";
	}
}
