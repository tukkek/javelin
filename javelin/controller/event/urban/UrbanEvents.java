package javelin.controller.event.urban;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.controller.db.StateManager;
import javelin.controller.event.EventCard;
import javelin.controller.event.EventDealer;
import javelin.controller.event.urban.basic.DegradeRelationship;
import javelin.controller.event.urban.basic.ImproveRelationship;
import javelin.controller.event.urban.basic.NothingHappens;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * Events that happens autonously per {@link Town} regardless of it being
 * hostile or not. Players will be notified of events in non-hostile
 * {@link Town}s regardless of having a {@link Squad} present or not. For
 * hostile towns, they are only notified if present.
 *
 * @author alex
 * @see Town#ishostile()
 */
public class UrbanEvents extends EventDealer{
	/**
	 * Singleton instance.
	 *
	 * @see StateManager
	 */
	public static UrbanEvents instance=new UrbanEvents();
	/**
	 * Used by {@link #newinstance(Class)} to be passed as a constructor argument.
	 */
	public static Town generating;

	private UrbanEvents(){
		positive
				.addcontent(List.of(ImproveRelationship.class,NothingHappens.class));
		neutral.addcontent(List.of(NothingHappens.class));
		negative.addcontent(
				List.of(DegradeRelationship.class,NothingHappens.class,Suspects.class));
	}

	@Override
	protected EventCard newinstance(Class<? extends EventCard> type)
			throws ReflectiveOperationException{
		return type.getConstructor(Town.class).newInstance(generating);
	}

	@Override
	protected EventDeck choosedeck(){
		var happiness=generating.describehappiness();
		List<EventDeck> choices;
		if(happiness==Town.HAPPY)
			choices=List.of(positive,positive,positive,neutral,negative);
		else if(happiness==Town.CONTENT)
			choices=List.of(positive,positive,neutral,negative);
		else if(happiness==Town.NEUTRAL)
			choices=List.of(positive,neutral,negative);
		else if(happiness==Town.UNHAPPY)
			choices=List.of(positive,neutral,negative,negative);
		else if(happiness==Town.REVOLTING)
			choices=List.of(positive,neutral,negative,negative,negative);
		else
			throw new RuntimeException("Unknown happines: "+happiness);
		return RPG.pick(choices);
	}

	@Override
	public String printsummary(String title){
		var info=new ArrayList<String>();
		info.add(positive.getcontentsize()+" positive");
		info.add(neutral.getcontentsize()+" neutral");
		info.add(negative.getcontentsize()+" negative");
		var types=new HashSet<Class<? extends EventCard>>();
		types.addAll(positive.getcontent());
		types.addAll(neutral.getcontent());
		types.addAll(negative.getcontent());
		var cards=new ArrayList<UrbanEvent>(types.size());
		var unknown=0;
		generating=null;
		for(var t:types)
			try{
				cards.add((UrbanEvent)newinstance(t));
			}catch(Exception e){
				unknown+=1;
			}
		info.add(cards.stream().filter(c->c.traits==null).count()+" basic");
		for(var t:Trait.ALL){
			var valid=cards.stream().filter(c->c.traits!=null&&c.traits.contains(t));
			info.add(valid.count()+" "+t);
		}
		info.add(unknown+" unknown");
		return types.size()+" "+title.toLowerCase()+" ("+String.join(", ",info)+")";
	}
}
