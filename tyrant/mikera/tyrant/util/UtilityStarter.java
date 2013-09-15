package tyrant.mikera.tyrant.util;

/**
 * 
 * @author Carsten Muessig <carsten.muessig@gmx.net>
 */

public class UtilityStarter {

	public static void main(String[] args) {

		// to do: enter the desired file path, example: c:\\myDir\\myFile.xml
		
		String prefix = "c:\\winme\\desktop\\temp";
		String path1 = "\\library_test.xml";
		PlugInUtility.writeLibrary(prefix+path1, true);
		//PlugInUtility.writeSpecification(path2);
		/*PlugInUtility.readPlugIn("c:\\winme\\desktop\\temp");
		boolean test1 = LibMetaData.isKnownProperty("HPS");
		boolean test2 = LibMetaData.isValidProperty("HPS", new Integer(100));
 		System.out.println("test1: "+test1);
		System.out.println("test2: "+test2);
		String property = LibMetaData.getPropertyDescription("HPS");
		System.out.println(property);
		*/
	}
}