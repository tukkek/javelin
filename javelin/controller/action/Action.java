package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * One of the game moves, to be used during battle.
 *
 * @see AiAction
 * @see ActionMapping
 * @author alex
 */
public abstract class Action implements Serializable,ActionDescription{
	private static final long serialVersionUID=1L;

	/** Movement */
	public static final Movement MOVE_NW=new Movement("Move (↖)",
			new String[]{"7","KeyEvent"+KeyEvent.VK_HOME,"U"},
			"↖ (7 on numpad, or U)");
	/** Movement */
	public static final Movement MOVE_N=new Movement("Move (↑)",
			new String[]{"8","KeyEvent"+KeyEvent.VK_UP,"I"},"↑ (8 on numpad, or I)");
	/** Movement */
	public static final Movement MOVE_NE=new Movement("Move (↗)",
			new String[]{"9","KeyEvent"+KeyEvent.VK_PAGE_UP,"O"},
			"↗ (9 on numpad, or O)");
	/** Movement */
	public static final Movement MOVE_W=new Movement("Move (←)",
			new String[]{"4","KeyEvent"+KeyEvent.VK_LEFT,"J","←"},
			"← (4 on numpad, or J)");
	/** Movement */
	public static final Movement MOVE_E=new Movement("Move (→)",
			new String[]{"6","KeyEvent"+KeyEvent.VK_RIGHT,"L","→"},
			"→ (6 on numpad, or L)");
	/** Movement */
	public static final Movement MOVE_SW=new Movement("Move (↙)",
			new String[]{"1","KeyEvent"+KeyEvent.VK_END,"M"},"↙ (1 in numpad, or M)");
	/** Movement */
	public static final Movement MOVE_S=new Movement("Move (↓)",
			new String[]{"2","KeyEvent"+KeyEvent.VK_DOWN,"<"},
			"↓ (2 in numpad, or <)");
	/** Movement */
	public static final Movement MOVE_SE=new Movement("Move (↘)",
			new String[]{"3","KeyEvent"+KeyEvent.VK_PAGE_DOWN,">"},
			"↘ (3 on numpad, or >)");

	/** How to present an action to the player. */
	public final String name;

	/** Action keys as strings. Useful for displaying to player too. */
	public String[] keys=new String[0];
	/** See {@link KeyEvent} constants. */
	public int[] keycodes=new int[0];
	/** Most actions cannot be performed while burrowed. */
	public boolean allowburrowed=false;

	/**
	 * @param key Transforms this to an array.
	 */
	public Action(final String name,final String key){
		this(name,new String[]{key});
	}

	/** Constructor. */
	public Action(final String name,final String[] keys){
		this(name);
		this.keys=keys;
	}

	/** Used for {@link AiAction}s with no keys. */
	public Action(final String name2){
		name=name2;
	}

	/** Constructor. */
	public Action(String namep,int[] keys){
		name=namep;
		keycodes=keys;
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @param dices Quantity of dice.
	 * @param sides Type of dice.
	 * @return Map of percentual chances mapped by total result (sum).
	 */
	static public TreeMap<Integer,Float> distributeroll(final int dices,
			final int sides){
		final int nCombinations=dices*sides;
		final int[][] combinations=new int[nCombinations][dices];
		for(int dice=0;dice<dices;dice++)
			for(int i=0,value=1,valueUse=0;i<nCombinations;i++){
				combinations[i][dice]=value;
				if(++valueUse==dice+1){
					valueUse=0;
					value=value==sides?1:value+1;
				}
			}
		final TreeMap<Integer,Integer> results=new TreeMap<>();
		for(final int[] combination:combinations){
			int sum=0;
			for(final int element:combination)
				sum+=element;
			final Integer ocurrences=results.get(sum);
			results.put(sum,(ocurrences==null?0:ocurrences)+1);
		}

		final TreeMap<Integer,Float> distribution=new TreeMap<>();
		for(final Entry<Integer,Integer> result:results.entrySet())
			distribution.put(result.getKey(),result.getValue()/(float)nCombinations);
		return distribution;
	}

	/**
	 * @param list The possible outcomes of the action decided by the AI.
	 * @param enableoverrun See {@link BattleScreen#setstate(ChanceNode, boolean)}
	 * @return The actual outcome of the action made by the AI.
	 */
	public static void outcome(final List<ChanceNode> list,boolean enableoverrun){
		float roll=RPG.random();
		for(final ChanceNode cn:list){
			roll-=cn.chance;
			if(roll<=0){
				if(cn.audio!=null) cn.audio.play();
				BattleScreen.active.setstate(cn,enableoverrun);
				return;
			}
		}
		for(final ChanceNode cn:list)
			System.err.println("Outcome error! "+cn.action+" "+cn.chance);
		throw new RuntimeException("Couldn't determine outcome: "+roll
				+debugteam(Fight.state.blueteam,"player team")
				+debugteam(Fight.state.redteam,"ai team"));
	}

	static String debugteam(ArrayList<Combatant> team,String label){
		String out="\n"+label+": ";
		for(Combatant c:team)
			out+=c.source.toString()+", ";
		return out;
	}

	@Override
	public String[] getDescriptiveKeys(){
		return keys;
	}

	@Override
	public String getDescriptiveName(){
		return name;
	}

	/**
	 * @return <code>true</code> if given character is listed in {@link #keys}.
	 */
	public boolean isPressed(final Character key){
		for(final String k:keys)
			if(k.equals(key.toString())) return true;
		return false;
	}

	/**
	 * Performs an action as the human player.
	 *
	 * @param active Current unit.
	 * @param m Current map.
	 * @param thing Unit's visual representation.
	 * @return <code>true</code> if executed an action (successfully or not). Just
	 *         return <code>false</code> if the player cannot use this action
	 *         (AI-only).
	 */
	public abstract boolean perform(Combatant active);

	/**
	 * Same as {@link #outcome(List, boolean)} but without overrun.
	 */
	public static void outcome(List<ChanceNode> list){
		outcome(list,false);
	}

	/**
	 * Emulates the fact that 1 in a d20 is always a fail and 20 is always a
	 * success.
	 *
	 * @return The given chance, bound to a 5%-95% range.
	 */
	public static float bind(float chance){
		if(chance>.95f) return .95f;
		if(chance<.05f) return .05f;
		return chance;
	}

	@Override
	public String getMainKey(){
		return keys.length==0?null:keys[0];
	}

	@Override
	public void setMainKey(String key){
		if(keys.length>0) keys[0]=key;
	}

	/**
	 * @return the chance of at least 1 out of 2 independent events happening,
	 *         given two percentage odds (1 = 100%).
	 */
	static public float or(float a,float b){
		return a+b-a*b;
	}
}
