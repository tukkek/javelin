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
import java.util.Map;
import java.util.TreeMap;

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
	 * Reverse of {@link #CACHE}, holds an Image's base filename (no extension or
	 * dir).
	 */
	public static final Map<Image,String> NAMES=new HashMap<>();

	static final GraphicsConfiguration ENVIRONMENT=GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();
	static final TreeMap<String,Image> CACHE=new TreeMap<>();
	static final TreeMap<String,Image> BURIEDCACHE=new TreeMap<>();

	/** @see {@link Combatant#ispenalized(javelin.model.state.BattleState)} */
	public static final Image PENALIZED=Images.maketransparent(3/4f,
			Images.get("overlaypenalized"));
	/** @see Location#hascrafted() */
	public static final Image CRAFTING=Images.get("overlaycrafting");
	/** @see Location#hasupgraded() */
	public static final Image UPGRADING=Images.get("overlayupgrading");
	/** @see Town#isworking() */
	public static final Image LABOR=Images.get("overlaylabor");
	/** Show while a {@link Meld} is being generated. */
	public static final Image DEAD=Images.get("overlaydead");
	/** Show when a {@link Meld} is generated. */
	public static final Image MELD=Images.get("overlaymeld");
	/** @see Location#ishostile() */
	public static final Image HOSTILE=Images.get("overlayhostile");
	/** @see Town#ishosting() */
	public static final Image TOURNAMENT=Images.get("locationtournament");
	/** Distinguishes {@link Combatant#mercenary} units. */
	public static final Image MERCENARY=Images.get("overlaymercenary");
	public static final Image SUMMONED=Images.get("overlaysummoned");
	public static final Image ELITE=Images.get("overlayelite");
	public static final Image TEXTUREMAP=Images.get("texturemap");

	/**
	 * @param combatant Unit to be shown.
	 * @return image resource.
	 */
	public static Image get(Combatant combatant){
		if(!combatant.burrowed) return get(combatant.source.avatarfile);
		final String avatar=combatant.source.avatarfile;
		Image buried=BURIEDCACHE.get(avatar);
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
