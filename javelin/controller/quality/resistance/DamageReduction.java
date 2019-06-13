package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A major point of damage reduction in d20 is that it can be entirely bypassed.
 * Since traditional equipment is not a feature of Javelin, it becomes
 * impossible to bypass damage reduction with normal attacks. Instead we apply
 * DR more like a monster's "hardness" (usually reserved for objects).
 *
 * For the same reason, damage reduction can never fully negate damage, only
 * reduce it to 1 point at most. Otherwise with a high enough DR value a unit
 * would literally become immune to damage - especially if a player doesn't have
 * hard-hitting characters and no access to damage upgrades.
 *
 * Having to deal with even 5 points of damage reduction without any ability to
 * bypass it has proven to be entirely too strong, especially when combined with
 * fast healing (such as for mephits). To counteract that we simply reduce all
 * absolute DR values to 1/3 (the "single element" equivalent from the CCR
 * document, by far the most common found in monsters.xml).
 *
 * At some point it may be necessary to more assertively apply the modifiers
 * from the CCR document, not just x1/3, but currently 100% of the monsters used
 * are single-element-only (either silver or magic).
 *
 * More info on the d20 SRD
 * http://www.d20srd.org/srd/specialAbilities.htm#damageReduction
 */
public class DamageReduction extends Quality{
	/** Applies {@link Monster#dr}. */
	public static final Upgrade UPGRADE=new DamageReductionUpgrade();

	/**
	 * See the d20 SRD for more info.
	 */
	static class DamageReductionUpgrade extends Upgrade{
		DamageReductionUpgrade(){
			super("Damage reduction");
		}

		@Override
		public String inform(Combatant m){
			return "Currently reducing "+m.source.dr+" points of damage";
		}

		@Override
		public boolean apply(Combatant m){
			final int hd=m.source.hd.count();
			double designparameter=5+Math.floor(hd/2f);
			final int max=Math.round(Math.round(designparameter/3));
			if(m.source.dr>=max) return false;
			m.source.dr=max;
			return true;
		}
	}

	public DamageReduction(){
		super("damage reduction");
	}

	@Override
	public void add(String declaration,Monster m){
		final int magicbonus=declaration.indexOf('/');
		if(magicbonus>=0) declaration=declaration.substring(0,magicbonus);
		final int dr=Integer.parseInt(declaration
				.substring(declaration.lastIndexOf(' ')+1,declaration.length()));
		m.dr=dr/3;
	}

	@Override
	public boolean has(Monster monster){
		return monster.dr>0;
	}

	@Override
	public float rate(Monster monster){
		return monster.dr*.1f;
	}

	@Override
	public String describe(Monster m){
		return "damage reduction "+m.dr;
	}
}
