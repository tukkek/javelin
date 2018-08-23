package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.controller.action.world.ShowOptions;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * Managers{@link AiThread}s.
 *
 * @author alex
 */
public class ThreadManager{
	static final Thread MAIN=Thread.currentThread();
	static final ArrayList<Integer> BATTLERECORD=new ArrayList<>();

	static final String PERFORMANCESOLUTION="The maximum number of AI threads has been lowered to %1$d.\n"
			+"This reduces the AI thinking power but increases speed.\n"
			+"You may need to go through this step a couple of times.\n\n"
			+"If you don't want to lose thinking power you can try\n"
			+"raising the thinking time manually via the Options screen.";
	static final String PERFORMANCENOTIFICATION="Your computer seems to be having trouble thinking fast enough.\n\n"
			+"The current configured thinking time is ~%1$d seconds.\n" //
			+"The current computer move has taken ~%2$d seconds to complete.\n\n"
			+"Press Yes to attempt an automatic solution.\n"
			+"Press No to ignore this message until the game is restarted.\n"
			+"Press Cancel to never see this message again.\n\n"
			+"You can also press %3$c after closing this dialog to configure your preferences manually.";

	static public void determineprocessors(){
		String message="\n\nThe AI will use "+Preferences.MAXTHREADS+" cores";
		if(Preferences.AICACHEENABLED) message+=" (cache enabled)";
		System.out.println(message);
		System.out.println();
	}

	public static Exception ERROR;
	public static boolean working;
	/**
	 * Increment by 1 if computation was done within proximity of
	 * {@link Preferences#MAXMILISECONDSTHINKING} or decrement by 1 otherwise.
	 * {@link Integer#MIN_VALUE} means no performance monitoring.
	 */
	public static int performance=0;

	public static List<ChanceNode> think(BattleState state){
		final long start;
		try{
			working=true;
			interrupt();
			TemperatureManager.cooldown();
			start=now();
			AiCache.clear();
			AiThread.depthincremeneter=1;
			AiThread.reset();
			int nthreads=Preferences.MAXTHREADS;
			long seed=RPG.rand.nextLong();
			for(int i=0;i<nthreads;i++)
				AiThread.STARTED.add(new AiThread(state,seed));
			sleep(start);
			interrupt();
			working=false;
		}catch(Exception e){
			interrupt();
			working=false;
			Javelin.message("Fatal error: "+ERROR.getMessage(),Javelin.Delay.NONE);
			ERROR.printStackTrace();
			throw new RuntimeException(ERROR);
		}
		int deepest=AiThread.FINISHED.descendingKeySet().first();
		analyze(start,deepest);
		return AiThread.FINISHED.get(deepest);
	}

	static void analyze(final long start,int depth){
		float miliseconds=now()-start;
		checkperformance(miliseconds);
		final float elapsed=miliseconds/1000f;
		System.out.println(elapsed+" seconds elapsed. Depth: "+depth);
		BATTLERECORD.add(depth);
	}

	static void sleep(final long start) throws InterruptedException,Exception{
		long sleepfor=Preferences.MAXMILISECONDSTHINKING-(now()-start);
		Thread.sleep(sleepfor>=0?sleepfor:0);
		while(AiThread.FINISHED.isEmpty()){
			if(ThreadManager.ERROR!=null) throw ThreadManager.ERROR;
			Thread.sleep(500);
		}
	}

	static void checkperformance(float miliseconds){
		if(performance==Integer.MIN_VALUE||Preferences.MAXTHREADS==1) return;
		performance+=miliseconds<Preferences.MAXMILISECONDSTHINKING+1000?+1:-1;
		if(performance>=-9) return;
		String message=String.format(PERFORMANCENOTIFICATION,
				Preferences.MAXMILISECONDSTHINKING/1000,Math.round(miliseconds/1000),
				ShowOptions.getsingleton().morekeys[0].charAt(0));
		int choice=JOptionPane.showConfirmDialog(BattleScreen.active,message,
				"Performance problem detected",JOptionPane.YES_NO_CANCEL_OPTION);
		if(choice==JOptionPane.YES_OPTION){
			performance=0;
			int to=Preferences.MAXTHREADS-1;
			Preferences.setoption(Preferences.KEYMAXTHREADS,to);
			JOptionPane.showMessageDialog(BattleScreen.active,
					String.format(PERFORMANCESOLUTION,to));
		}else if(choice==JOptionPane.NO_OPTION)
			performance=Integer.MIN_VALUE;
		else if(choice==JOptionPane.CANCEL_OPTION){
			performance=Integer.MIN_VALUE;
			Preferences.savefile(
					Preferences.getfile()+"\n"+Preferences.KEYCHECKPERFORMANCE+"=false");
		}else if(choice!=-1&&Javelin.DEBUG)
			throw new RuntimeException("#performancereply "+choice);
	}

	static boolean interrupting;

	public static void interrupt(){
		interrupting=true;
		AiThread.group.interrupt();
		for(Thread t:new ArrayList<>(AiThread.STARTED))
			try{
				t.join();
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		destroy();
		AiThread.group=AiThread.regroup();
		interrupting=false;
	}

	/**
	 * There is some sort of weird JRE bug that spawns a class loader thread (as
	 * part of {@link AiThread#group}, since that's the current context) and it
	 * doesn't stop itself gracefully when calling
	 * {@link ThreadGroup#interrupt()}. As such, it dangles on our group on some
	 * weird cases and causes this call to throw an {@link IllegalStateException}.
	 *
	 * Sadly, this is out of our scope to fix (to the best of my understanding) so
	 * it's better not to destroy the group if that thread is still running.
	 *
	 * The exact point the TimerThread is instantited is on the first call to
	 * {@link BattleState#getsucessors()}, which then causes
	 * {@link ActionProvider} to be loaded by the {@link ClassLoader}.
	 *
	 * Thankfully, as far as I can tell, the problem solves itself pretty fast
	 * (probably the garbage collector cleaning the thread/group by itself).
	 * Still, would be ncie to eventually be able to skip this check and destroy
	 * the group every single time, which had been working normally for literal
	 * years before this issue.
	 *
	 * @author alex
	 */
	static void destroy(){
		if(AiThread.group.activeCount()==0) AiThread.group.destroy();
	}

	static private long now(){
		return new Date().getTime();
	}

	static public void printbattlerecord(){
		if(BATTLERECORD.isEmpty()) return;
		Collections.sort(BATTLERECORD);
		System.out.println(
				"Median depth for battle: "+BATTLERECORD.get(BATTLERECORD.size()/2));
		System.out.println();
		BATTLERECORD.clear();
	}
}
