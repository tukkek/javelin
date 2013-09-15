//package javelin.controller.db.reader;
//
//
//class Skills extends FieldReader {
//	/**
//	 * 
//	 */
//	private final MonsterReader monsterReader;
//
//	Skills(MonsterReader monsterReader, final String fieldname) {
//		super(fieldname);
//		this.monsterReader = monsterReader;
//	}
//
//	@Override
//	void read(final String value) {
//		if (value.length() == 0) {
//			for (final String s : value.split(", ")) {
//				final int split = s.lastIndexOf(" ");
//				this.monsterReader.monster.addSkill(s.substring(0, split),
//						s.substring(split + 1).replaceAll("\\*", ""));
//			}
//		}
//	}
// }