package javelin.controller.ai.cache;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.AlphaBetaSearch;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.db.Preferences;

/**
 * Prevents utility ({@link BattleAi#utility(Node)}) and successors (
 * {@link AiAction#getoutcomes(javelin.model.state.BattleState, javelin.model.unit.Combatant)}
 * ) from having to be recalculated by different {@link AiThread}s.
 *
 * @author alex
 */
public class AiCache{
	static final boolean DEBUG=false;

	public static Cache nodecache=new Cache();
	public static Cache utilitycache=new Cache();

	public static void clear(){
		if(!Preferences.aicacheenabled) return;
		if(DEBUG){
			nodecache._findlargest();
			utilitycache._findlargest();
		}
		nodecache.clear();
		utilitycache.clear();
	}

	public static Iterable<List<ChanceNode>> getcache(final Node previousState,
			List<Integer> index){
		if(!Preferences.aicacheenabled) return previousState.getsucessors();
		List<List<ChanceNode>> sucessors=(List<List<ChanceNode>>)nodecache
				.get(index);
		if(sucessors==null){
			sucessors=new ArrayList<>();
			for(final List<ChanceNode> cns:previousState.getsucessors())
				sucessors.add(cns);
			addcache(index,sucessors);
		}
		return sucessors;
	}

	private static void addcache(List<Integer> index,
			List<List<ChanceNode>> sucessors){
		ArrayList<List<ChanceNode>> copy=new ArrayList<>(sucessors.size());
		for(List<ChanceNode> list:sucessors){
			list=new ArrayList<>(list);
			for(int i=0;i<list.size();i++)
				list.set(i,list.get(i).clone());
			copy.add(list);
		}
		nodecache.put(index,copy);
	}

	public static Float getutility(ArrayList<Integer> index,
			final AlphaBetaSearch ai,final List<ChanceNode> cns){
		if(!Preferences.aicacheenabled) return ai.utility(cns);
		Float utility=(Float)utilitycache.get(index);
		if(utility==null){
			utility=ai.utility(cns);
			utilitycache.put(index,utility);
		}
		return utility;
	}

	public static void reset(){
		nodecache.root=new Link();
		utilitycache.root=new Link();
	}
}
