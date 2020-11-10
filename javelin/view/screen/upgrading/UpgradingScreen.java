package javelin.view.screen.upgrading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.feat.Feat;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * Lets a plear {@link Upgrade} members from a {@link Squad}.
 *
 * Upgrading 1 level (100XP) should take 1 week and cost $50.
 *
 * @author alex
 */
public abstract class UpgradingScreen extends SelectScreen{
	public class UpgradeOption extends Option{
		/** Upgrade in question. */
		public final Upgrade u;

		/** Constructor. */
		public UpgradeOption(final Upgrade u){
			super(u.name,0);
			this.u=u;
			Spell s=u instanceof Spell?(Spell)u:null;
			if(s!=null){
				name=name.toLowerCase();
				name="Spell: "+name+" (level "+s.level+")";
				priority=2;
			}
		}

		@Override
		public double sort(){
			return u instanceof Spell?((Spell)u).level:super.sort();
		}
	}

	final HashMap<Integer,Combatant> original=new HashMap<>();
	final HashSet<Combatant> upgraded=new HashSet<>();
	protected boolean showmoneyinfo=true;
	/**
	 * When <code>true</code> and if possible, will use the only eligible trainee
	 * instead of showing the selection prompt. This is OK for {@link Minigame}s
	 * but other than that, the prompt is useful to see XP and gold prices,
	 * upgrade details, etc.
	 *
	 * TODO shouldn't be necessary with 2.0
	 *
	 * @see Upgrade#inform(Combatant)
	 */
	protected boolean skipselection=false;
	boolean returntoselection;
	boolean quitselection;

	/**
	 * Constructor.
	 *
	 * @param t Can be <code>null</code>, only {@link TownUpgradingScreen} depends
	 *          on it.
	 */
	public UpgradingScreen(String name,Town t){
		super(name,t);
	}

	@Override
	public void show(){
		if(original.isEmpty()) for(Combatant c:gettrainees())
			original.put(c.id,c.clone().clonesource());
		super.show();
	}

	/**
	 * @param trainee Unit that has been taken out of it's {@link Squad} for
	 *          training.
	 */
	protected abstract void registertrainee(Order trainee);

	/**
	 * Mostly concerned with {@link Squad} clean-up issues.
	 *
	 * @param trainees
	 */
	protected void onexit(ArrayList<TrainingOrder> trainees){
		// nothing by default
	}

	/** Available {@link UpgradeOption}s. */
	protected abstract Collection<Upgrade> getupgrades();

	@Override
	public boolean select(final Option op){
		final UpgradeOption o=(UpgradeOption)op;
		final List<Combatant> eligible=new ArrayList<>();
		String listeligible=listeligible(o,eligible);
		if(eligible.isEmpty()){
			print(text+"\nNone can learn this right now...\n");
			return false;
		}
		Combatant c;
		if(eligible.size()==1&&skipselection)
			c=eligible.get(0);
		else{
			c=selecttrainee(eligible,listeligible);
			if(returntoselection) return false;
			if(quitselection) return true;
		}
		finishpurchase(o,c);
		return true;
	}

	Combatant selecttrainee(final List<Combatant> eligible,String prompt){
		returntoselection=false;
		quitselection=false;
		final String parenttext=text;
		text+=prompt;
		if(showmoneyinfo)
			text+="Your squad has $"+Javelin.format(getgold())+".\n\n";
		text+="Which squad member? Press r to return to upgrade selection.";
		Javelin.app.switchScreen(this);
		while(true){
			repaint();
			try{
				final Character input=InfoScreen.feedback();
				if(input=='r'){
					text=parenttext;
					returntoselection=true;
					return null;
				}
				if(input==PROCEED){
					quitselection=true;
					return null;
				}
				return eligible.get(Integer.parseInt(input.toString())-1);
			}catch(final NumberFormatException e){
				continue;
			}catch(final IndexOutOfBoundsException e){
				continue;
			}
		}
	}

	void finishpurchase(final UpgradeOption o,Combatant c){
		if(buy(o,c,false)!=null){
			update(c);
			upgraded.add(c);
			c.postupgrade();
		}
	}

	void update(Combatant c){
		for(Feat f:c.source.feats)
			f.update(c);
	}

	String listeligible(final UpgradeOption o,final List<Combatant> eligible){
		String s="\n";
		int i=1;
		for(final Combatant c:gettrainees()){
			String name=c.toString();
			while(name.length()<=10)
				name+=" ";
			final BigDecimal cost=buy(o,c.clone().clonesource(),true);
			if(cost!=null&&cost.compareTo(new BigDecimal(0))>0
					&&c.xp.compareTo(cost)>=0){
				eligible.add(c);
				String costinfo="    Cost: "
						+cost.multiply(new BigDecimal(100)).setScale(0,RoundingMode.HALF_UP)
						+"XP, $"+price(cost.floatValue());
				s+="["+i+++"] "+name+" "+o.u.inform(c)+costinfo;
				Integer days=getperiod(cost.floatValue());
				if(days!=null) s+=", "+days+" days\n";
			}
		}
		return s+"\n";
	}

	protected Integer getperiod(float cost){
		return Math.round(cost*TrainingOrder.UPGRADETIME);
	}

	abstract public ArrayList<Combatant> gettrainees();

	private int price(float xp){
		return Math.round(xp*50);
	}

	private BigDecimal buy(final UpgradeOption o,Combatant c,boolean listing){
		float originalcr=ChallengeCalculator.calculaterawcr(c.source)[1];
		final Combatant clone=c.clone().clonesource();
		if(!upgrade(o,clone)) return null;
		BigDecimal cost=new BigDecimal(
				ChallengeCalculator.calculaterawcr(clone.source)[1]-originalcr);
		if(!listing){
			int goldpieces=price(cost.floatValue());
			if(goldpieces>getgold()){
				text+="\n\nNot enough gold! Press any key to continue...";
				print(text);
				InfoScreen.feedback();
				return null;
			}
			pay(goldpieces);
		}
		upgrade(o,c);
		c.xp=c.xp.subtract(cost);
		ChallengeCalculator.calculatecr(c.source);
		return cost;
	}

	abstract public int getgold();

	abstract public void pay(int goldpieces);

	protected boolean upgrade(final UpgradeOption o,final Combatant c){
		return o.u.upgrade(c);
	}

	@Override
	public String printinfo(){
		return "";
	}

	@Override
	public String getCurrency(){
		return "XP";
	}

	@Override
	public List<Option> getoptions(){
		Collection<Upgrade> upgrades=getupgrades();
		ArrayList<Option> options=new ArrayList<>(upgrades.size());
		for(Upgrade u:upgrades)
			if(u.showupgrade()) options.add(createoption(u));
		return options;
	}

	protected UpgradeOption createoption(Upgrade u){
		return new UpgradeOption(u);
	}

	@Override
	protected Comparator<Option> sort(){
		return (a,b)->{
			if(a.priority!=b.priority) return Double.compare(a.priority,b.priority);
			if(a.sort()!=b.sort()) return Double.compare(a.sort(),b.sort());
			return a.name.compareTo(b.name);
		};
	}

	@Override
	public String printpriceinfo(Option o){
		return "";
	}

	@Override
	public void onexit(){
		ArrayList<TrainingOrder> trainees=new ArrayList<>();
		for(Combatant c:upgraded){
			Combatant original=this.original.get(c.id);
			float xpcost=ChallengeCalculator.calculaterawcr(c.source)[1]
					-ChallengeCalculator.calculaterawcr(original.source)[1];
			trainees.add(createorder(c,original,xpcost));
		}
		onexit(trainees);
		for(TrainingOrder trainee:trainees)
			registertrainee(trainee);
	}

	abstract public TrainingOrder createorder(Combatant c,Combatant original,
			float xpcost);

}