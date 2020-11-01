package javelin.view;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javelin.Debug;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;

/**
 * Cache for {@link Monster#avatar}
 *
 * @author alex
 */
public class Images{
	/**
	 * Reverse of {@link #CACHE}, holds an Image's relative path without
	 * extension..
	 */
	public static final Map<Image,String> NAMES=new HashMap<>();

	static final GraphicsConfiguration ENVIRONMENT=GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();
	static final HashMap<String,Image> CACHE=new HashMap<>();
	static final HashMap<String,Image> BURIEDCACHE=new HashMap<>();

	/** @see {@link Combatant#ispenalized(javelin.model.state.BattleState)} */
	public static final Image PENALIZED=Images.maketransparent(3/4f,
			Images.get(List.of("overlay","penalized")));
	/** @see Location#hascrafted() */
	public static final Image CRAFTING=Images.get(List.of("overlay","crafting"));
	/** @see Location#hasupgraded() */
	public static final Image UPGRADING=Images
			.get(List.of("overlay","upgrading"));
	/** @see Town#isworking() */
	public static final Image LABOR=Images.get(List.of("overlay","labor"));
	/** Show while a {@link Meld} is being generated. */
	public static final Image DEAD=Images.get(List.of("overlay","dead"));
	/** Show when a {@link Meld} is generated. */
	public static final Image MELD=Images.get(List.of("overlay","meld"));
	/** @see Location#ishostile() */
	public static final Image HOSTILE=Images.get(List.of("overlay","hostile"));
	/** @see Town#ishosting() */
	public static final Image TOURNAMENT=Images
			.get(List.of("world","tournament"));
	/** Distinguishes {@link Combatant#mercenary} units. */
	public static final Image MERCENARY=Images
			.get(List.of("overlay","mercenary"));
	public static final Image SUMMONED=Images.get(List.of("overlay","summoned"));
	public static final Image ELITE=Images.get(List.of("overlay","elite"));
	public static final Image TEXTUREMAP=Images.get("texturemap");

	static String topath(List<String> path){
		return String.join(File.separator,path);
	}

	/**
	 * @param combatant Unit to be shown.
	 * @return image resource.
	 */
	public static Image get(Combatant combatant){
		var path=topath(List.of("monster",combatant.source.avatarfile));
		if(!combatant.burrowed) return get(path);
		var avatar=path;
		var buried=BURIEDCACHE.get(avatar);
		if(buried==null){
			buried=maketransparent(1/3f,get(avatar));
			BURIEDCACHE.put(avatar,buried);
		}
		return buried;
	}

	/**
	 * This should be the preferred method of loading images because this way it's
	 * friendlier for modding purposes.
	 *
	 * @param file PNG extension is added.
	 * @return An image from the avatar folder.
	 */
	synchronized public static Image get(final String file){
		Image i=CACHE.get(file);
		if(i!=null) return i;
		try{
			var raw=ImageIO.read(new File("avatars"+File.separator+file+".png"));
			var w=raw.getWidth(null);
			var h=raw.getHeight(null);
			i=ENVIRONMENT.createCompatibleImage(w,h,Transparency.TRANSLUCENT);
			i.getGraphics().drawImage(raw,0,0,null);
			CACHE.put(file,i);
			NAMES.put(i,file);
			return i;
		}catch(IOException e){
			var dir=" (pwd="+System.getProperty("user.dir")+")";
			throw new RuntimeException(file+dir,e);
		}
	}

	/** @return Same as {@link #get(List)} but using a path. */
	public static Image get(List<String> path){
		return get(topath(path));
	}

	/**
	 * @param alpha Alpha level. 1 is 100% opaque, 0 is 100% transparent.
	 */
	public static Image maketransparent(float alpha,Image image){
		BufferedImage transparent=ENVIRONMENT.createCompatibleImage(32,32,
				Transparency.TRANSLUCENT);
		Graphics2D g=transparent.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		g.drawImage(image,0,0,null);
		g.dispose();
		return transparent;
	}

	/** @see Debug */
	public static void clearcache(){
		CACHE.clear();
		BURIEDCACHE.clear();
	}
}
