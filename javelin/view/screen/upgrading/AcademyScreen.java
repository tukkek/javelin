package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.view.screen.Option;

/**
 * @see MartialAcademy
 * @author alex
 */
public class AcademyScreen extends UpgradingScreen{
	class Pillage extends Option{
		Pillage(Fortification f){
			super("Pillage ($"+Javelin.format(f.getspoils())+")",0,'p');
			priority=4;
		}
	}

	protected Academy academy;

	/** Constructor. */
	public AcademyScreen(Academy academy,Town t){
		super(academy.descriptionknown,t);
		this.academy=academy;
		stayopen=true;
		//TODO skip confirmation since it's always 1 hero
	}

	@Override
	protected void registertrainee(Order trainee){
		academy.training.add(trainee);
		Combatant c=((TrainingOrder)trainee).trained;
		Squad.active.equipment.remove(c);
		Squad.active.remove(c);
	}

	@Override
	protected void onexit(ArrayList<TrainingOrder> trainees){
		if(Squad.active.members.size()==trainees.size()){
			academy.stash+=Squad.active.gold;
			if(academy.parking==null
					||Squad.active.transport.price>academy.parking.price)
				academy.parking=Squad.active.transport;
		}
	}

	@Override
	protected ArrayList<Upgrade> getupgrades(){
		return new ArrayList<>(academy.upgrades);
	}

	@Override
	public List<Option> getoptions(){
		List<Option> options=super.getoptions();
		if(academy.pillage&&ChallengeCalculator
				.calculateel(Squad.active.members)>academy.targetel)
			options.add(new Pillage(academy));
		return options;
	}

	@Override
	public boolean select(Option op){
		if(op instanceof Pillage){
			academy.pillage();
			return true;
		}
		return super.select(op);
	}

	@Override
	public String printinfo(){
		String training=academy.training.queue.isEmpty()?""
				:"Currently training: "+academy.training+".";
		return "Your squad currently has $"+Javelin.format(Squad.active.gold)+". "
				+training;
	}

	@Override
	public TrainingOrder createorder(Combatant c,Combatant original,float xpcost){
		return new TrainingOrder(c,Squad.active.equipment.get(c),c.toString(),
				xpcost,original);
	}

	@Override
	public ArrayList<Combatant> gettrainees(){
		return Squad.active.members;
	}

	@Override
	public int getgold(){
		return Squad.active.gold;
	}

	@Override
	public void pay(int goldpieces){
		Squad.active.gold-=goldpieces;
	}
}