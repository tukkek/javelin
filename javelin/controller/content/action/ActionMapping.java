package javelin.controller.content.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javelin.controller.content.action.ai.AiMovement;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.controller.content.action.ai.attack.RangedAttack;
import javelin.controller.content.action.maneuver.ExecuteManeuver;
import javelin.controller.content.action.world.WorldAction;
import javelin.controller.content.action.world.meta.Automate;
import javelin.controller.content.action.world.meta.OpenJournal;
import javelin.controller.content.action.world.meta.ShowOptions;
import javelin.controller.content.action.world.meta.help.Guide;
import javelin.controller.exception.RepeatTurn;

/**
 * Basic listing of in-game {@link Action}s.
 *
 * @author alex
 * @see WorldAction
 */
public class ActionMapping{
	private static final boolean LONGMOVE=true;
	/** Canonical array of possible battle actions. */
	public static final Action[] ACTIONS=new Action[]{ //
			new AutoAttack(),Breath.SINGLETON, // b
			new Charge(), // c
			Dig.SINGLETON, // d
			Fire.SINGLETON, // f
			new CastSpell(), // s
			UseItem.SINGLETON, // i
			new ActionAdapter(OpenJournal.getsingleton()), // o
			new ActionAdapter(ShowOptions.getsingleton()), // o
			PassItem.SINGLETON, // p
			new TouchAttack(), // t
			Defend.SINGLETON, // w
			new Examine(), // x
			ExecuteManeuver.INSTANCE, // m

			new ActionAdapter(new Automate()), // A
			new ConfigureBattleKeys(), // K
			new Withdraw(), // W

			new ZoomOut(), // -
			new ZoomIn(), // +

			new ActionAdapter(Guide.HOWTO),new ActionAdapter(Guide.MINIGAMES),
			new ActionAdapter(Guide.ARTIFACTS),new ActionAdapter(Guide.CONDITIONS),
			new ActionAdapter(Guide.ITEMS),new ActionAdapter(Guide.SKILLS),
			new ActionAdapter(Guide.SPELLS),new ActionAdapter(Guide.UGRADES),
			new ActionAdapter(Guide.DISTRICT),new ActionAdapter(Guide.KITS),
			new ActionAdapter(Guide.DISCIPLINES),new ActionAdapter(Guide.QUESTIONS), // //

			new Help(), // h

			Action.MOVE_N,Action.MOVE_NE,Action.MOVE_E,Action.MOVE_SE,Action.MOVE_S,
			Action.MOVE_SW,Action.MOVE_W,Action.MOVE_NW,AiMovement.SINGLETON,
			MeleeAttack.INSTANCE,RangedAttack.INSTANCE,};
	/** Only instance of this class. */
	public static final ActionMapping SINGLETON=new ActionMapping();
	/** If true will reload keys. */
	public static boolean reset=false;

	final Map<String,Action> mappings=new HashMap<>();

	private ActionMapping(){
		setup();
	}

	/**
	 * Register {@link Action#keys} and {@link Action#keycodes}.
	 */
	public void setup(){
		for(final Action a:ACTIONS){
			for(final String key:a.keys)
				if(mappings.put(key,a)!=null) throw new RuntimeException(
						"Key conflict ("+key+") registering action "+a.name);
			for(int code:a.keycodes)
				if(mappings.put("KeyEvent"+code,a)!=null) throw new RuntimeException(
						"Key conflict ("+code+") registering action "+a.name);
		}
	}

	/**
	 * @return The appropriate acttion for the given {@link KeyEvent}.
	 */
	public Action getaction(final KeyEvent keyEvent){
		if(ActionMapping.reset){
			mappings.clear();
			setup();
		}
		final char keyChar=keyEvent.getKeyChar();
		if(keyChar==KeyEvent.CHAR_UNDEFINED){
			final Action action=mappings.get("KeyEvent"+keyEvent.getKeyCode());

			if(action==null) throw new RepeatTurn();

			return action;
		}
		// Make sure we can control-something events
		if(keyEvent.isControlDown()) // Not sure the puritain JDK will like this
			// Sun should sue those Eclipse guys
			return mappings.get("Control"+keyEvent.getKeyCode());
		Action action=mappings.get(Character.toString(keyChar));
		if(action==null) throw new RepeatTurn();
		return action;
	}
}
