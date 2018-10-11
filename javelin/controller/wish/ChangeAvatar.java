package javelin.controller.wish;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Changes the image used to represent a {@link Combatant}.
 *
 * @see Monster#avatarfile
 * @author alex
 */
public final class ChangeAvatar extends Wish{
	static final FilenameFilter FILTER=(dir,name)->name.toLowerCase()
			.endsWith(".png");

	/** Constructor. */
	public ChangeAvatar(Character keyp,WishScreen s){
		super("change unit avatar",keyp,1,true,s);
	}

	@Override
	boolean wish(Combatant target){
		HashSet<String> avatars=new HashSet<>();
		try{
			File avatarfolder=new File("avatars");
			if(avatarfolder.isDirectory())
				for(String avatar:avatarfolder.list(FILTER)){
					avatar=avatar.substring(0,avatar.length()-4).trim();
					if(!avatar.isEmpty()) avatars.add(avatar);
				}
		}catch(Exception e){
			e.printStackTrace();
		}
		ArrayList<String> alphabetical=new ArrayList<>(avatars);
		Collections.sort(alphabetical);
		int delta=0;
		while(delta<alphabetical.size()){
			screen.text="";
			for(int i=delta;i<delta+9&&i<alphabetical.size();i++)
				screen.text+="["+(i-delta+1)+"] "+alphabetical.get(i)+"\n";
			screen.text+="\nPress ENTER to see more options or a number to select a new avatar.";
			Character feedback=screen.print();
			if(feedback=='\n'){
				delta+=9;
				if(delta>=alphabetical.size()) delta=0;
				continue;
			}
			try{
				final int index=Integer.parseInt(Character.toString(feedback));
				target.source.avatarfile=alphabetical.get(delta+index-1);
				break;
			}catch(NumberFormatException e){
				continue;
			}catch(IndexOutOfBoundsException e){
				continue;
			}
		}
		return true;
	}
}