package eu.gpirro.utilities;


public class TriplePattern {

	String sub;
	String pred;
	String obj;
	String direction;

	public TriplePattern() {

	}

	public TriplePattern(String sub, String pred, String obj, String direction) {
		super();
		this.sub = sub;
		this.pred = pred;
		this.obj = obj;
		this.direction = direction;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getPred() {
		return pred;
	}

	public void setPred(String pred) {
		this.pred = pred;
	}

	public String getObj() {
		return obj;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}

	public TriplePattern invertTriplePattern() {
		TriplePattern res = null;

		if (getDirection() == Constants.DIRECTION_FW)
			res = new TriplePattern(getObj(), getPred(), getSub(),
					Constants.DIRECTION_BW);
		else if (getDirection() == Constants.DIRECTION_BW)
			res = new TriplePattern(getObj(), getPred(), getSub(),
					Constants.DIRECTION_FW);

		return res;

	}

	@Override
	public String toString() {
		return getSub() + " " + getPred() + " " + getObj() + ".";
	}
}
