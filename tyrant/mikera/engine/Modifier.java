
package tyrant.mikera.engine;

import java.util.*;

import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Scripts;


/**
 * @author Mike
 *
 * The modifier class implements temporary modifiers
 * to the properties of any Thing
 * 
 * Interacts very closely with the Thing.get(...) method
 *  
 * Note: Modifier is final for performance reasons
 * 
 */
public final class Modifier extends BaseObject {
	private static final long serialVersionUID = -3261034883675606102L;
    private Thing source;
	private String reason;
	
	private Modifier() {
		super();
	}
	
	private Modifier(Modifier m) {
		super(m);
	}
	
	public static final Comparator sorter=new ModifierSorter();
	/**
	 * Implements sort in modifier priority order
	 */ 
	private static class ModifierSorter implements Comparator {
		public int compare(Object a, Object b) {
			Modifier ma=(Modifier)a;
			Modifier mb=(Modifier)b;
			return mb.getPriority()-ma.getPriority();
		}
	}
	
	public static Modifier create(Modifier base, Thing source, String s) {
		Modifier m=new Modifier(base);
		m.source=source;
		m.reason=s;
		return m;
	}
	
	public String getStat() {
		return getString("Stat");
	}
	
	public String getReason() {
		return reason;
	}
	
	//public void set(String s, Object o) {
	//	throw new Error("Tying to change modifier! ("+s+"="+o.toString()+")");
	//}
	
	public int getPriority() {
		return getStat("Priority");
	}
	
	public Thing getSource() {
		return source;
	}

	// this is the main modifier calculation function
	public Object calculate(Thing t, String s, int pos) {

        if (getFlag("Calculation")) {
            return new Integer(calculation(t,s,pos));
		} 
		
		else if (getFlag("Override")) {
			return get("Value");
		} 
		
		else if (getFlag("Scripted")) {
			Event e=new Event("CalculateScriptedModifier");
			e.set("Result",t.getModified(s,pos));
			e.set("Target",t);
			e.set("Source",source);
			Script script=getScript("Script");
			script.handle(t,e);
			return e.get("Result");
		} 
		
		else if (getFlag("AddModifier")) {
			Modifier[] mods=(Modifier[])t.getModified(s,pos);
			Modifier m=(Modifier) get("Value");
			if (mods==null) return new Modifier[] {m};
			Modifier[] nmods=new Modifier[mods.length+1];
			System.arraycopy(mods,0,nmods,0,mods.length);
			nmods[mods.length]=m;
			return nmods;
		}
		
		else if (getFlag("AddHandler")) {
			Script sc=(Script)t.getModified(s,pos);
			Script ns=(Script) get("Value");
			Script combined=Scripts.combine(sc,ns);
			return combined;
		}
		
		return t.getModified(s,pos);
	}
	
	public int calculation(Thing t, String s, int pos) {

        int modifierBase = getStat("Base");
		int multiplier = getStat("Multiplier");
		int bonus = getStat("Bonus");
		Integer statBase = (Integer)t.getModified(s,pos);
		if (statBase == null) {
            if (modifierBase > 0) {
                return ((modifierBase * multiplier) / 100) + bonus;
            }
            return bonus;
        }
        return ((statBase.intValue() * multiplier) / 100) + bonus;
	}
	
	public static Modifier bonus(String s, int bonus) {
		return linear(s,100,bonus);
	}
	
	/**
	 * Creates a simple stat modifier
	 * 
	 * @param stat Stat name e.g. "SK"
	 * @param modifier Multiplier as a percentage
	 * @param bonus Bonus to be applied (after multiplier)
	 * @return new modifier object
	 */
	public static Modifier linear(String stat, int percentage, int bonus) {
		Modifier mod=new Modifier();
		mod.set("Stat",stat);
		mod.set("Calculation",1);
		mod.set("Multiplier",percentage);
		mod.set("Bonus",bonus);
		mod.set("Priority",100);
		return mod;
	}
	

    public static Modifier percent(String stat,int percent) {
        Modifier modifier = new Modifier();
        modifier.set("Stat",stat);
        modifier.set("Base",100);
        modifier.set("Calculation",1);
        modifier.set("Multiplier",percent);
        modifier.set("Priority",100);
        return modifier;
    }

    public static Modifier constant(String s, int v) {
		return constant(s,new Integer(v));
	}
	
	/**
	 *
	 * Creates a constant value modifier 
	 * this overrides any previosly set value
	 * 
	 */
	public static Modifier constant(String s, Object v) {
		Modifier mod=new Modifier();
		mod.set("Stat",s);
		mod.set("Override",1);
		mod.set("Value",v);
		mod.set("Priority",100);	
		return mod;
	}
	
	public static Modifier scripted(String s, Script script) {
		Modifier mod=new Modifier();
		mod.set("Stat",s);
		mod.set("Scripted",1);
		mod.set("Script",script);
		mod.set("Priority",100);	
		return mod;
		
	}
	
	public static Modifier addModifier(String s, Object v) {
		Modifier mod=new Modifier();
		mod.set("Stat",s);
		mod.set("AddModifier",1);
		mod.set("Value",v);
		mod.set("Priority",100);	
		return mod;
	}
	
	public static Modifier addHandler(String s, Script v) {
		Modifier mod=new Modifier();
		mod.set("Stat",s);
		mod.set("AddHandler",1);
		mod.set("Value",v);
		mod.set("Priority",100);	
		return mod;
	}
	
	public String toString() {
		String s= getStat();
		if (getFlag("Calculation")) {
			if (getStat("Bonus")!=0) s=s+"+"+getStat("Bonus");
		}
		return s;
	}
}
