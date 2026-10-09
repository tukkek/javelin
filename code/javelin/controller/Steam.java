package javelin.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.system.Library;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamLibraryLoader;

import javelin.Debug;

/// Uses `steamworks4j` to integrate with Steam.
///
/// TODO at some point will need to nandle 3 releases:
/// * No Steam
/// * Steam-demonstration (#5393530)
/// * Steam (#5259370)
///
/// The best-extensible way to do this is probably to have a `steam.json` (or
/// omit it for no Steam).
public class Steam{
  /// If `false` all methods should be nooperations.
  public static final boolean ENABLED=System.getenv("SteamAppId")!=null
      &&System.getenv("SteamGameId")!=null;
  /// Singleton
  public static final Steam INSTANCE=new Steam();

  static final int IDENTITY=5393530;

  class Tick extends TimerTask{
    @Override
    public void run(){
      if(SteamAPI.isSteamRunning())
        com.codedisaster.steamworks.SteamAPI.runCallbacks();
    }
  }

  /// patch our own lwjgl3 loader. needed as steamworks4j-lwjgl3 breaks modules.
  class Loader implements SteamLibraryLoader{
    private String libraryPath;

    @Override
    public void setLibraryPath(String libraryPath){
      this.libraryPath=libraryPath;
    }

    @Override
    public boolean loadLibrary(String libraryName){
      try{
        if(libraryPath!=null&&!libraryPath.trim().isEmpty()){
          // If a explicit path is set, load directly from that location
          var platformLibName=System.mapLibraryName(libraryName);
          var libFile=new File(libraryPath,platformLibName);
          System.load(libFile.getAbsolutePath());
        }else // Leverage LWJGL 3's library loading mechanism
          // Respects LWJGL system properties (e.g. org.lwjgl.librarypath)
          Library.loadSystem("com.codedisaster.steamworks",libraryName);
        return true;
      }catch(Throwable t){
        System.err.println("Failed to load Steam native library: "+libraryName);
        t.printStackTrace();
        return false;
      }
    }
  }

  /// Load Steam-libraries
  public void load(){
    try{
      if(!ENABLED) return;
      var loader=new Loader();
      if(!SteamAPI.loadLibraries(loader))
        throw new RuntimeException("Failed to load Steam-libraries.");
      if(SteamAPI.restartAppIfNecessary(IDENTITY)) System.exit(0);
      if(!SteamAPI.init())
        throw new RuntimeException("Failed to integrate Steam.");
      new Timer().scheduleAtFixedRate(new Tick(),0,1_000/20);
      Debug.log("Steam integrated.",getClass());
    }catch(SteamException e){
      return;
    }
  }

  /// Close Steam with grace.
  public void close(){
    if(ENABLED) SteamAPI.shutdown();
  }
}
