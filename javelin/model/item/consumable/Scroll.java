package javelin.model.item.consumable;

import java.security.InvalidParameterException;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;

/**
 * {@link Scroll}s are a catch-all, spell-completion item type for
 * non-{@link #iswand} {@link Spell}s. Scrolls provoke attacks of opportunity
 * and require spell-completion capabilities.
 *
 * @author alex
 */
public class Scroll extends Item{
	/** Contains one instance of each type of spell. */
	public static final HashSet<Scroll> SCROLLS=new HashSet<>();
	/** Spell this scroll can cast once. */
	public final Spell spell;

	/**
	 * @param s The Spell this scroll casts.
	 * @see Item#Item(String, int, ItemSelection)
	 */
	public Scroll(final Spell s){
		super("Scroll of "+s.name.toLowerCase(),
				s.level*s.casterlevel*50+s.components,true);
		if(Javelin.DEBUG&&!s.isscroll&&!s.provokeaoo)
			throw new InvalidParameterException();
		spell=s.clone();
		usedinbattle=s.castinbattle;
		usedoutofbattle=s.castoutofbattle;
		apcost=0;
		identified=false;
		SCROLLS.add(this);
	}

	@Override
	public boolean use(Combatant user){
		CastSpell.SINGLETON.cast(spell,user);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant c){
		failure=null;
		if(!read(c)){
			failure=c+" needs more experience before reading this scroll.";
			return false;
		}
		if(!spell.validate(c,null)) return false;
		spell.castpeacefully(c);
		return true;
	}

	@Override
	public boolean equals(Object obj){
		return super.equals(obj)&&name.equals(((Scroll)obj).name);
	}

	@Override
	public String describefailure(){
		return failure==null?super.describefailure():failure;
	}

	@Override
	public String canuse(Combatant c){
		return read(c)?null:"can't read";
	}

	/**
	 * @return <code>true</code> if can read a {@link Spell} from a
	 *         {@link Scroll}.
	 */
	public boolean read(Combatant c){
		if(c.taketen(Skill.USEMAGICDEVICE)>=10+spell.casterlevel) return true;
		int spellcraft=Skill.SPELLCRAFT.getranks(c);
		return c.decipher(spell)
				&&10+c.source.hd.count()+spellcraft/2>=spell.casterlevel+1;
	}

	@Override
	public boolean identify(Combatant c){
		return read(c)||super.identify(c);
	}
}
