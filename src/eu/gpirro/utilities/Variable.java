package eu.gpirro.utilities;

public class Variable implements Comparable {
	private String varName;

	public Variable(String newVarName) {
		varName = newVarName;
	}

	@Override
	public int compareTo(Object anObj) {
		if (anObj instanceof Variable) {
			Variable var = (Variable) anObj;

			int thisNumber = 0;
			int otherNumber = 0;
			if (varName.startsWith("pred")) {
				thisNumber = Integer.parseInt(varName.substring(6));
			} else
				thisNumber = Integer.parseInt(varName.substring(3));

			if (var.getVarName().startsWith("pred")) {
				otherNumber = Integer.parseInt(var.getVarName().substring(6));
			} else
				otherNumber = Integer.parseInt(var.getVarName().substring(3));

			if (thisNumber != otherNumber)
				return thisNumber - otherNumber;
			else
				return varName.startsWith("pred") ? -1 : 1;
		} else
			return -1;
	}

	/**
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * @param varName
	 *            the varName to set
	 */
	public void setVarName(String varName) {
		this.varName = varName;
	}

}
