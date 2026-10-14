package javelin.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.db.Preferences;
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
   * Tritonus doesn't seem to work with Linux 64b. TODO JavaFX
   */
  static final List<String> EXTENSIONS=List.of(".wav");
  static final Map<String,String> PLAYERS=new LinkedHashMap<>();

  static String player=null;

  static{
    PLAYERS.put("phiola","phiola play -volume %volume %path");
    PLAYERS.put("pw-cat","pw-cat --playback %path");
    PLAYERS.put("cmdmp3.exe","cmdmp3.exe %path");
    var paths=Arrays
        .stream(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
        .map(Path::of).collect(Collectors.toList());
    var windows=System.getProperty("os.name").toLowerCase().contains("win");
    for(var command:PLAYERS.keySet()){
      var found=paths.stream().map(dir->dir.resolve(command)).anyMatch(
          file->Files.isRegularFile(file)&&(windows||Files.isExecutable(file)));
      if(!found) continue;
      player=command;
      break;
    }
  }

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

  /** <code>null</code> constructor. */
  public Audio(String action){
    this(action,(Monster)null);
  }

  File lookup() throws NoAudio{
    List<String> names=List.of(action);
    if(monster!=null){
      var byname=action+"-"+monster.name.replaceAll(" ","");
      var bytype=action+"-"+monster.type;
      names=List.of(byname,bytype,action);
    }
    for(var filename:names){
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
      var command=PLAYERS.get(player);
      command=command.replace("%volume",Preferences.volume);
      command=command.replace("%path",lookup().toString());
      new ProcessBuilder(command.split(" ")).start();
    }catch(Exception e){
      if(DEBUG) throw new RuntimeException(e);
    }
  }

  @Override
  public String toString(){
    return action+" ("+monster+")";
  }
}
