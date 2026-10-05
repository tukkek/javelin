package javelin.model.unit.feat.save;

import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class IronWill extends SaveFeat{
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON=new IronWill();

	private IronWill(){
		super("iron will");
	}

	@Override
	public void setbonus(final Monster m,int value){
		m.setwill(value);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Integer getbonus(final Monster m){
		return m.will;
	}

	@Override
	protected String getname(){
		return "will";
	}

}
