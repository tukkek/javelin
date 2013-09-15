package tyrant.mikera.engine;

import tyrant.mikera.tyrant.util.Text;

/**
 * Code for implementing object descriptions
 * 
 * @author Mike
 *
 */
public class Describer implements Description, java.io.Serializable {
	// Description variables

	private static final long serialVersionUID = 4050196462069954612L;
    protected String description;
	protected String name;
	protected String pluralname;
	protected int nametype;
	protected int gender;

	public Describer(String n, String pn, String d, int nt, int gen) {
		name = n;
		pluralname = pn;
		description = d;
		nametype = nt;
		gender = gen;
	}

	public Describer(String n, String d) {
		this(n, null, d, NAMETYPE_NORMAL, GENDER_NEUTER);
	}

	public Describer(String n, String pn, String d) {
		this(n, pn, d, NAMETYPE_NORMAL, GENDER_NEUTER);
	}

	public Describer(String n, String pn, int nt, String d) {
		this(n, pn, d, nt, GENDER_NEUTER);
	}

	public String getName(int number, int article) {
		switch (article) {
			case ARTICLE_NONE :
				return getName(number);
			default :
				String s = getArticle(number, (article == ARTICLE_DEFINITE));
				return (s == null) ? getName(number) : s + " "
						+ getName(number);
		}
	}

	public String getName(int number) {
		if (number == NUMBER_SINGULAR) {
			return name;
		}
        return (pluralname != null) ? pluralname : (name + "s");
	}

	public String getDescriptionText() {
		return (description == null)
				? (Text.capitalise(name) + ".")
				: description;
	}

	public String getArticle(int number, boolean definite) {
		switch (nametype) {
			case NAMETYPE_PROPER :
				return null;
			case NAMETYPE_NORMAL :
				if (number == NUMBER_SINGULAR)
					return definite ? "the" : (Text.isVowel(name.charAt(0))
							? "an"
							: "a");
			// otherwise continue to next case
			case NAMETYPE_QUANTITY :
				return definite ? "the" : "some";
		}
		return "*article error*";
	}

	// static function to return normal articles. Useful for other Description
	// classes
	public static String getArticleName(String s, int number, int article) {
		if (article == ARTICLE_NONE)
			return s;
		if (article == ARTICLE_POSSESIVE)
			return "your "+s;
		if (number == NUMBER_SINGULAR)
			return (article == ARTICLE_DEFINITE) ? "the " + s : (Text.isVowel(s
					.charAt(0)) ? "an " : "a ")
					+ s;
		// otherwise we have a normal plural
		return (article == ARTICLE_DEFINITE) ? "the " + s : "some " + s;
	}

	public String getPronoun(int number, int acase) {
		return getPronoun(number, acase, nametype, gender);
	}

	public static String getPronoun(int number, int acase, int nametype,
			int gender) {
		if (number == NUMBER_SINGULAR) {
			if ((nametype == NAMETYPE_PROPER) && (gender != GENDER_NEUTER)) {
				switch (acase) {
					case CASE_NOMINATIVE :
						return (gender == GENDER_MALE) ? "he" : "she";
					case CASE_ACCUSATIVE :
						return (gender == GENDER_MALE) ? "him" : "her";
					case CASE_GENITIVE :
						return (gender == GENDER_MALE) ? "his" : "her";
				}
			} else {
				switch (acase) {
					case CASE_NOMINATIVE :
						return "it";
					case CASE_ACCUSATIVE :
						return "it";
					case CASE_GENITIVE :
						return "its";
				}
			}
		} else {
			if (nametype == NAMETYPE_QUANTITY) {
				switch (acase) {
					case CASE_NOMINATIVE :
						return "it";
					case CASE_ACCUSATIVE :
						return "it";
					case CASE_GENITIVE :
						return "its";
				}
			} else {
				switch (acase) {
					case CASE_NOMINATIVE :
						return "they";
					case CASE_ACCUSATIVE :
						return "them";
					case CASE_GENITIVE :
						return "their";
				}
			}
		}
		return "*Pronoun Error*";
	}

	public static String getArticle(String name, int number, int nametype,
			int article) {
		
		if ((number==1)&&(article==ARTICLE_NONE)) return null;
		
		// capital implies proper noun
		if (Character.isUpperCase(name.charAt(0))) return null;
		
		switch (nametype) {
			case NAMETYPE_PROPER :
				return null;

			case NAMETYPE_NORMAL :
				if (number == 1) {
					if (article == ARTICLE_DEFINITE)
						return "the";
					if (article == ARTICLE_POSSESIVE)
						return "your";
					if (article == ARTICLE_INDEFINITE)
						return (Text.isVowel(name.charAt(0)) ? "an" : "a");
					return null;
				}
                if (article == ARTICLE_DEFINITE)
                    return "the "+Integer.toString(number);
                if (article == ARTICLE_POSSESIVE)
                    return "your "+Integer.toString(number);
                return Integer.toString(number);
			case NAMETYPE_QUANTITY :
				if (number == 1) {
					if (article==ARTICLE_POSSESIVE) return "your";
					return (article == ARTICLE_DEFINITE) ? "the" : "some";
				}
				return Integer.toString(number);					
			}
		throw new Error("*article error* [" + name + "]");
	}

	public static String getName(Thing t) {
		return getName(t, t.getNumber());
	}

	public static String getName(Thing item, int number) {
		String result;
		if (number == 1) {
			String n= item.getSingularName();
			result=(n==null)?"no name":n;
		} else {
			String n= item.getPluralName();
			result=(n==null)?"no names":n;
		}
		// get adjective list, including ending space
		String adj=item.getAdjectives();
		if (adj!=null) result=adj+result;
		return result;
	}

	public static String describe(Thing person, Thing item) {
		return describe(person, item, ARTICLE_NONE);
	}

	public static boolean isPlural(Thing t) {
		return (t.getNumber()>1)||(t.getStat("NameType")==NAMETYPE_QUANTITY);
	}
	
	public static String describe(Thing person, Thing item, int article) {
        int number = item.getNumber();
		return describe(person, item, article, number);
	}

	public static String describe(Thing person, Thing item, int article, int number) {
		if (item.isHero()) return "you";
		int nametype = item.getStat("NameType");
		String name = getName(item);
		if ((item.place!=null)&&(person!=null)&&(!item.isVisible(person))) {
			return (number==1)?"something":"some things";
		}
		String s = getArticle(name, number, nametype, article);
		return (s == null) ? getName(item, number) : s + " " + getName(item, number);
	}
}