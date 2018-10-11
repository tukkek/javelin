package javelin.model.item.relic;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * TODO placeholder for real relics
 *
 * @author alex
 */
public abstract class Relic extends Item{
	static int RECHARGEPERIOD=24*7;
	long lastused=-RECHARGEPERIOD;

	/** Constructor. */
	public Relic(String name,int templelevel){
		super(name,RewardCalculator.getgold(templelevel),null);
		consumable=false;
		waste=false;
		provokesaoo=false;
	}

	@Override
	public boolean use(Combatant user){
		if(!usedinbattle) super.use(user);
		userelic(user);
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant user){
		if(!usedoutofbattle) super.use(user);
		return userelic(user);
	}

	boolean userelic(Combatant user){
		if(charge()){
			String text="The "+name+" is recharging and can't be used right now...";
			if(BattleScreen.active instanceof WorldScreen){
				Javelin.app.switchScreen(BattleScreen.active);
				Javelin.message(text,false);
			}else
				Javelin.message(text,Javelin.Delay.BLOCK);
			return true;
		}
		if(!activate(user)) return false;
		lastused=Squad.active.hourselapsed;
		return true;
	}

	boolean charge(){
		return Squad.active.hourselapsed-lastused<RECHARGEPERIOD;
	}

	/**
	 * Invoke the relics's power.
	 *
	 * @param user Unit handling the relic.
	 * @return <code>false</code> if the use is cancelled by the player.
	 */
	protected abstract boolean activate(Combatant user);

	@Override
	public String canuse(Combatant c){
		return charge()?"recharging":null;
	}
}
