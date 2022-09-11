package eu.gpirro.utilities;

import java.util.Arrays;


/**
 * INNER CLASS COMBINATION
 * 
 * @author gpirro
 *
 */
public class Combination {
	private String all;
	private int nbPredicate;
	private boolean[] isReversed;

	public Combination(String newAll, int newNbPredicate,
			boolean[] newIsReserved) {
		all = newAll;
		nbPredicate = newNbPredicate;
		setIsReversed(newIsReserved);
	}

	/**
	 * @return the all
	 */
	public String getAll() {
		return all;
	}

	/**
	 * @return the isReversed
	 */
	public boolean[] getIsReversed() {
		return isReversed;
	}

	public boolean getIsReversed(int i) {
		return isReversed[i];
	}

	/**
	 * @return the nbPredicate
	 */
	public int getNbPredicate() {
		return nbPredicate;
	}

	/**
	 * @param all
	 *            the all to set
	 */
	public void setAll(String all) {
		this.all = all;
	}

	/**
	 * @param isReversed
	 *            the isReversed to set
	 */
	public void setIsReversed(boolean[] isReversed) {
		this.isReversed = Arrays.copyOf(isReversed, isReversed.length);
	}

	/**
	 * @param nbPredicate
	 *            the nbPredicate to set
	 */
	public void setNbPredicate(int nbPredicate) {
		this.nbPredicate = nbPredicate;
	}

	@Override
	public String toString() {
		return all;
	}
}