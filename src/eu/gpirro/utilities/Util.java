package eu.gpirro.utilities;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author
 */
public class Util {

	public static String getObjectFullName(String url) {

		for (int i = 0; i < Constants.namespaces_keys.length; i++) {

			url = url.replaceAll(Constants.namespaces_keys[i],
					Constants.namespaces_values[i]);

		}

		return "<" + url + ">";
		/*
		 * System.out.println(url); String[] listObject = url.split("/"); String
		 * object = listObject[listObject.length - 1].replace("_", " ");
		 * 
		 * // return "dbp:"+object; return object;
		 */
	}

	public static String getObjectShortName(String url) {

		for (int i = 0; i < Constants.namespaces_keys.length; i++) {

			url = url.replaceAll(Constants.namespaces_values[i],
					Constants.namespaces_keys[i]);

		}

		return url;
		/*
		 * System.out.println(url); String[] listObject = url.split("/"); String
		 * object = listObject[listObject.length - 1].replace("_", " ");
		 * 
		 * // return "dbp:"+object; return object;
		 */
	}

	public static String allDiff(int objNumber, String firstObject,
			String lastObject) {
		String[] listObject = new String[objNumber + 2];
		// listObject[0] = firstObject;
		// listObject[objNumber + 1] = lastObject;
		listObject[0] = "'"
				+ firstObject.substring(1, firstObject.length() - 1) + "'";
		listObject[objNumber + 1] = "'"
				+ lastObject.substring(1, lastObject.length() - 1) + "'";
		for (int i = 1; i <= objNumber; i++) {
			int minus = i - 1;
			listObject[i] = "str(?obj" + minus + ")";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < objNumber; i++) {
			for (int j = i + 2; j < objNumber + 2; j++) {
				// sb.append(" && !sameTerm(" + listObject[i] + "," +
				// listObject[j] + ")");
				sb.append(" && " + listObject[i] + "!=" + listObject[j]);
			}
		}

		return sb.toString();
	}

}
