package javelin.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Looks up, loads, caches and plays audio.
 *
 * Given an action A, the filename lookup for a given {@link Monster} is in the
 * order: "A-name", A-type, "A".
 *
 * TODO this should probably be entirely scrapper in favor of JavaFX internal
 * media player
 *
 * @author alex
 */
public class Audio{
	static final boolean DEBUG=Javelin.DEBUG&&false;
	/**
	 * Cannot easily support cross-platform MP3 and OGG with any up-to-date.
	 * Tritonus doesn't seem to work with Linux 64b. TODO JaveFX
	 */
	static final List<String> EXTENSIONS=List.of(".wav");
	static final String PLAYER="fmedia";

	class NoAudio extends IOException{
		public NoAudio(Throwable cause){
			super(cause);
		}

		public NoAudio(String message){
			super(message);
		}
	}

	String action;
	Monster monster;

	/** Constructor. */
	public Audio(String action,Monster m){
		this.action=action;
		monster=m;
	}

	/** Constructor. */
	public Audio(String action,Combatant c){
		this(action,c.source);
	}

	File lookup() throws NoAudio{
		var byname=action+"-"+monster.name.replaceAll(" ","");
		var bytype=action+"-"+monster.type;
		for(var filename:List.of(byname,bytype,action)){
			filename=filename.toLowerCase();
			for(var extension:EXTENSIONS){
				var f=new File("audio",filename+extension);
				if(f.exists()) return f;
			}
		}
		throw new NoAudio("Cannot find audio for "+this);
	}

	/** Plays the audio. */
	public void play(){
		try{
			var path=lookup().getCanonicalPath();
			Runtime.getRuntime().exec(new String[]{PLAYER,path});
		}catch(Exception e){
			if(DEBUG) throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(){
		return action+" ("+monster+")";
	}
}
