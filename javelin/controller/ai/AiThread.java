package javelin.controller.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import javelin.controller.content.action.ai.AiAction;
import javelin.controller.exception.StopThinking;
import javelin.model.state.BattleState;
import javelin.old.RPG;

/**
 * Efficiently uses a CPU core to run an {@link AlphaBetaSearch}. When the
 * search is done it starts a new one using the same thread, to avoid creating
 * more than threads than there are CPUs available.
 *
 * @author alex
 */
public class AiThread extends Thread{
	public static final TreeMap<Integer,List<ChanceNode>> FINISHED=new TreeMap<>();
	static final ArrayList<Thread> STARTED=new ArrayList<>();
	/** TODO https://github.com/tukkek/javelin/issues/266 */
	static final int MAX_DEPTH=100;

	public static ThreadGroup group=regroup();
	/**
	 * @see #setdepth()
	 */
	static int depthincremeneter;

	public BattleState state;
	public int depth;
	List<ChanceNode> result;
	final Random random;
	BattleAi ai;

	public AiThread(BattleState state,long randomseed){
		super(group,(Runnable)null);
		this.state=state.clonedeeply();
		random=new Random(randomseed);
		setdepth();
		start();
	}

	public static ThreadGroup regroup(){
		return new ThreadGroup("Javelin AI threads");
	}

	@Override
	public void run(){
		try{
			ai=new BattleAi(depth);
			result=ai.alphaBetaSearch(state);
			onend();
		}catch(StopThinking e){
			// abort
		}catch(Exception e){
			ThreadManager.ERROR=e;
		}
	}

	/**
	 * Synchronously assigns a new {@link #depth} to this thread.
	 */
	synchronized public void setdepth(){
		depth=depthincremeneter;
		if(depth>=MAX_DEPTH) throw new StopThinking();
		setName("Javelin AI "+depth);
		depthincremeneter+=1;
	}

	synchronized private void onend(){
		if(Thread.interrupted()) return;
		AiThread.FINISHED.put(depth,result);
		if(!ThreadManager.interrupting&&ThreadManager.working&&!terminate()){
			setdepth();
			run();
		}
	}

	private boolean terminate(){
		for(ChanceNode c:result)
			if(!ai.terminalTest(c.n)) return false;
		return true;
	}

	static public void checkinterrupted(){
		if(Thread.interrupted()||!ThreadManager.working||ThreadManager.interrupting)
			if(((AiThread)Thread.currentThread()).depth!=1) throw new StopThinking();
	}

	public static synchronized void reset(){
		ThreadManager.interrupt();
		AiThread.FINISHED.clear();
		AiThread.STARTED.clear();
	}

	/**
	 * This allows {@link BattleAi} to achieve some defree of randomization. This
	 * cannto be used to determine action outcomes though because, even if proven
	 * to be internally reliable, it would allow the AI to know the outcomes of
	 * dice rolls during the thinking phase, instead of having to force it to
	 * think in possibilites instead.
	 *
	 * This is offered in the hopes that, as long as the same seed is given at
	 * {@link AiThread#AiThread(BattleState, long)}, the same sequence of calls to
	 * {@link AiAction#getoutcomes(javelin.model.unit.attack.Combatant, BattleState)}
	 * will produce reliable results. This, however, cannot be entirely guaranteed
	 * given how prunning may work in different {@link #depth} calculations.
	 *
	 * TODO this has not been guaranteed to produce deterministic results when it
	 * comes to different {@link #depth}s and such but is a relatively relaible
	 * tool that can be used for less important {@link AiAction}s.
	 *
	 * @return A RNG that is bound to the context of this thread. If called from
	 *         outside an {@link AiThread}, returns {@link RPG#rand} isntead.
	 */
	public static Random getrandom(){
		Thread t=Thread.currentThread();
		if(t instanceof AiThread) return ((AiThread)t).random;
		return RPG.rand;
	}
}