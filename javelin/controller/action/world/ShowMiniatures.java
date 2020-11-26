package javelin.controller.action.world;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.collection.CountingSet;
import javelin.model.Miniatures;
import javelin.view.screen.WorldScreen;

public class ShowMiniatures extends WorldAction{
	public ShowMiniatures(){
		super("Show miniature collection",new int[]{'M'},new String[]{"M"});
	}

	@Override
	public void perform(WorldScreen s){
		if(Miniatures.miniatures.isEmpty()){
			Javelin.message("Your miniature collection is empty...",false);
			return;
		}
		var list=new CountingSet();
		list.comparator=(a,b)->a.compareTo(b);
		list.casesensitive=true;
		list.addall(Miniatures.miniatures);
		var screen="Miniature collection:\n\n";
		screen+=list.getorderedelements().stream().sequential()
				.map(m->list.getcount(m)+"x "+m).collect(Collectors.joining("\n"));
		Javelin.promptscreen(screen);
	}
}
