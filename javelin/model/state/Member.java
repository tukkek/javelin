package javelin.model.state;

import java.io.Serializable;

public class Member implements Serializable {
	public float cr;
	public String type;
	public String name;

	public Member(final float cr, final String type, final String name) {
		super();
		this.cr = cr;
		this.type = type;
		this.name = name;
	}
}
