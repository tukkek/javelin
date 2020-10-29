package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.LoreNote;
import javelin.view.frame.text.TextScreen;
import javelin.view.screen.WorldScreen;

/**
 * Shows all {@link LoreNote}s collected by the player.
 *
 * @author alex
 */
public class ShowLore extends WorldAction{
	static final boolean DEBUG=false;

	class LoreScreen extends TextScreen{
		public LoreScreen(){
			super("Known lore");
			readonly=true;
		}

		@Override
		protected void savetext(String string){
			// don't, uses Dungeon#lore
		}

		@Override
		protected String loadtext(){
			var text=new ArrayList<String>();
			var dungeons=Dungeon.getdungeonsandtemples();
			dungeons.sort((a,b)->{
				var tiera=a.gettier().tier.minlevel;
				var tierb=b.gettier().tier.minlevel;
				return tiera!=tierb?tierb-tiera:a.toString().compareTo(b.toString());
			});
			for(var d:dungeons){
				var lore=d.lore.stream().filter(l->l.discovered||Javelin.DEBUG&&DEBUG)
						.map(l->"- "+l.text).sorted().collect(Collectors.toList());
				if(!lore.isEmpty()) text.add(d+":\n"+String.join("\n",lore));
			}
			return text.isEmpty()?"No lore discovered yet..."
					:String.join("\n\n",text);
		}
	}

	/** Constructor. */
	public ShowLore(){
		super("Show known lore",new int[]{'l'},new String[]{"l"});
	}

	@Override
	public void perform(WorldScreen screen){
		var s=new LoreScreen();
		s.show();
		s.defer();
	}
}
