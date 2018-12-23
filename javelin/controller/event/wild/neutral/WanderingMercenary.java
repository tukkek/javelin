package javelin.controller.event.wild.neutral;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.wild.Wanderer;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;
import javelin.model.world.location.unique.MercenariesGuild;

/**
 * Event where a random, similarly-leveled mercenary offers its services.
 *
 * @author alex
 */
public class WanderingMercenary extends Wanderer{
	static final String HIRE="Hire";
	static final String DECLINE="Decline";
	static final String ATTACK="Attack!";

	static final class CrDifferenceComparator implements Comparator<Monster>{
		double squadcr;

		private CrDifferenceComparator(double squadcr){
			this.squadcr=squadcr;
		}

		@Override
		public int compare(Monster a,Monster b){
			return Double.compare(Math.abs(a.cr-squadcr),Math.abs(b.cr-squadcr));
		}
	}

	Monster mercenary;
	Terrain terrain;

	/** Reflection-friendly constructor. */
	public WanderingMercenary(PointOfInterest l){
		super("Mercenary",l);
	}

	@Override
	public void happen(Squad s){
		var fee=Javelin.format(MercenariesGuild.getfee(mercenary));
		var prompt="A wandering "+mercenary.toString().toLowerCase()
				+" mercenary is willing to join you for $"+fee+"/day. Do you accept?";
		List<String> options=List.of(HIRE,DECLINE,ATTACK);
		var choice=options.get(Javelin.choose(prompt,options,true,true));
		if(choice==DECLINE) return;
		var mercenary=new Combatant(this.mercenary,true);
		if(choice==ATTACK) throw new StartBattle(new EventFight(mercenary,location));
		if(choice==HIRE){
			mercenary.setmercenary(true);
			s.add(mercenary);
		}else
			throw new UnsupportedOperationException("Unknown #findmercenary option");
	}

	@Override
	public boolean validate(Squad s,int el){
		if(!super.validate(s,el)) return false;
		var squadcr=Squad.active.members.stream()
				.collect(Collectors.averagingDouble(c->c.source.cr));
		terrain=Terrain.get(location.getlocation().x,location.getlocation().y);
		mercenary=select(squadcr,terrain);
		return mercenary!=null;
	}

	/**
	 * Test-friendly.
	 *
	 * @param squadcr Given an average party level,
	 * @param t and a terrain type
	 * @return An intelligent {@link Monster} of similar CR.
	 */
	static Monster select(double squadcr,Terrain t){
		var candidates=t.getmonsters().stream().filter(m->m.think(-1))
				.collect(Collectors.toList());
		if(candidates.isEmpty()) return null;
		var target=candidates.stream()
				.collect(Collectors.minBy(new CrDifferenceComparator(squadcr)))
				.get().cr;
		Collections.shuffle(candidates);
		return candidates.stream().filter(m->m.cr.equals(target.floatValue()))
				.findAny().get();
	}

	/**
	 * @return A string summary of Mercenaries found for level 1-20 on all
	 *         terrains.
	 */
	public static String test(){
		String result="";
		var maxdifference=-Float.MAX_VALUE;
		var sumdifference=0;
		for(var terrain:Terrain.ALL){
			result+=terrain.toString().toUpperCase()+"\n";
			for(var level=1;level<=20;level++){
				var mercenary=WanderingMercenary.select(level,terrain);
				result+="Level "+level+": "+mercenary+" (CR "+mercenary.cr+")\n";
				var difference=Math.max(maxdifference,Math.abs(mercenary.cr-level));
				maxdifference=difference;
				sumdifference+=difference;
			}
			result+="\n";
		}
		return "Max level difference: "+maxdifference+"\n"
				+"Average level difference: "+sumdifference/(20*Terrain.ALL.length)
				+"\n\n"+result;
	}
}
