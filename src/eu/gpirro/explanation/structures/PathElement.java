package eu.gpirro.explanation.structures;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author
 */
public class PathElement {
	private String url;
	private String shortName;

	public PathElement(String newUrl, String newShortName) {
		url = newUrl;
		shortName = newShortName;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName
	 *            the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	@Override
	public String toString() {

		return "URL=" + url + " shortName=" + shortName;

	}

}
