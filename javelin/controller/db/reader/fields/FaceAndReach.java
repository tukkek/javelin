package javelin.controller.db.reader.fields;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class FaceAndReach extends FieldReader {

	public FaceAndReach(MonsterReader monsterReader, final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	public void read(final String value) {
		final String faceAndReach = value.replaceAll(" by ", "x")
				.replaceAll(" ft.", "").replace(" /", "/").replace("/ ", "/")
				.replaceAll(" 1/2", ".5");
		if (faceAndReach.equals("5x5/5")) {
			// monsterReader.monster.face = new Point(5, 5);
			// monsterReader.monster.reach = 5;
		} else {
			reader.errorhandler.setInvalid("ComplexFace:" + faceAndReach);
		}
	}
}