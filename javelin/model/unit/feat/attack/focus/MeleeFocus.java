package javelin.model.unit.feat.attack.focus;

import java.util.List;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.Feat;

/**
 * @see WeaponFocus
 * @author alex
 */
public class MeleeFocus extends WeaponFocus{
	/** Unique instance of this {@link Feat}. */
	public static final MeleeFocus SINGLETON=new MeleeFocus();

	private MeleeFocus(){
		super("Melee focus");
	}

	@Override
	protected List<AttackSequence> getattacks(final Monster m){
		return m.melee;
	}

}
