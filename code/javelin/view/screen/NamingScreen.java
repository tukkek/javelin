package javelin.view.screen;

public class NamingScreen{
	/**
	 * @param currentName Given an object's current name...
	 * @return a new user-inputted name for it.
	 */
	static public String getname(final String currentName){
		String nametext="Give a new name to "+currentName+": ";
		final IntroScreen namescreen=new IntroScreen(nametext);
		String name="";
		char f;
		while(true){
			f=InfoScreen.feedback();
			if(f=='\n') if(!name.isEmpty()) break;
			if(!(f=='\b'||f==' '||Character.isLetterOrDigit(f))) continue;
			if(f=='\b'){
				if(!name.isEmpty()) name=name.substring(0,name.length()-1);
			}else
				name=name+f;
			namescreen.text=nametext+name;
			namescreen.repaint();
		}
		return name;
	}
}
