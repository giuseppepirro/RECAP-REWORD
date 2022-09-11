package eu.gpirro.utilities;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Find the URI that better approximates a given string in input
 * 
 * @author
 */
public class URIFInder {
	String query;
	String[] listURL;

	public URIFInder(String newQuery) {
		query = newQuery;
		listURL = new String[20];
	}

	private void getTopResults(int nbResult) throws Exception {
		int start = 0;
		boolean finish = false;
		listURL = new String[nbResult * 2];

		int idxListURL = 0;
		do {
			String myIP = "87.0.11.28";
			String myKey = "AIzaSyC7qZLnR4YBJnac7UaFvvqDpirPonqKRwE";

			String a = query + " site:dbpedia.org/resource";
			// String
			// s="http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=PREFIX+dbpedia%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F%3E+PREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E+PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E+SELECT+*+WHERE+{%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FPortugal%3E+%3FpredTo0+%3Fobj0+.+%3Fobj0+%3FpredTo1+%3Fobj1+.+%3Fobj1+%3FpredTo2+%3Fobj2+.+%3Fobj2+%3FpredTo3+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FItaly%3E+.+FILTER+%28!sameTerm%28%3FpredTo0%2Crdf%3Atype%29+%26%26+!sameTerm%28%3FpredTo0%2Crdfs%3AsubClassOf%29+%26%26+!sameTerm%28%3FpredTo1%2Crdf%3Atype%29+%26%26+!sameTerm%28%3FpredTo1%2Crdfs%3AsubClassOf%29+%26%26+!sameTerm%28%3FpredTo2%2Crdf%3Atype%29+%26%26+!sameTerm%28%3FpredTo2%2Crdfs%3AsubClassOf%29+%26%26+!sameTerm%28%3FpredTo3%2Crdf%3Atype%29+%26%26+!sameTerm%28%3FpredTo3%2Crdfs%3AsubClassOf%29+%26%26+!isLiteral%28%3Fobj0%29+%26%26+!regex%28str%28%3Fobj0%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FList%27%29+%26%26+!regex%28str%28%3Fobj0%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3A%27%29+%26%26+!regex%28str%28%3Fobj0%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FTemplate%3A%27%29+%26%26+!regex%28str%28%3Fobj0%29%2C%27^http%3A%2F%2Fsw.opencyc.org%2F%27%29+%26%26+!isLiteral%28%3Fobj1%29+%26%26+!regex%28str%28%3Fobj1%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FList%27%29+%26%26+!regex%28str%28%3Fobj1%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3A%27%29+%26%26+!regex%28str%28%3Fobj1%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FTemplate%3A%27%29+%26%26+!regex%28str%28%3Fobj1%29%2C%27^http%3A%2F%2Fsw.opencyc.org%2F%27%29+%26%26+!isLiteral%28%3Fobj2%29+%26%26+!regex%28str%28%3Fobj2%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FList%27%29+%26%26+!regex%28str%28%3Fobj2%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3A%27%29+%26%26+!regex%28str%28%3Fobj2%29%2C%27^http%3A%2F%2Fdbpedia.org%2Fresource%2FTemplate%3A%27%29+%26%26+!regex%28str%28%3Fobj2%29%2C%27^http%3A%2F%2Fsw.opencyc.org%2F%27%29+%29+.}+LIMIT+10%0D%0A&debug=on&timeout=600000&format=application%2Fsparql%2Dresults%2Bjson&save=display&fname=";

			URI uri = new URI("https", "ajax.googleapis.com",
					"/ajax/services/search/web", "v=1.0&q=" + a
							+ "&hl=en&start=" + start + "&key=" + myKey
							+ "&userip=" + myIP, null);

			// URL url = new URL(s);
			URL url = new URL(uri.toASCIIString());
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer", "http://joy.cc");

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			JSONObject jTotal = new JSONObject(builder.toString());

			JSONObject jObject = jTotal.getJSONObject("responseData");
			JSONArray jArray = jObject.getJSONArray("results");

			for (int i = 0; i < jArray.length() && idxListURL < nbResult; i++) {
				listURL[idxListURL] = ((JSONObject) jArray.get(i))
						.getString("url");
				idxListURL++;
			}

			if (idxListURL == nbResult)
				finish = true;
			else
				start = idxListURL;
		} while (!finish);
	}

	public void getTopResults2(int nbResult) {
		// TODO: here
		StringBuilder sb = new StringBuilder();

		String queryString = "";
		queryString = queryString
				.concat("SELECT ?s count(?s) as ?count as ?count WHERE {")
				.concat("?someobj ?p ?s . ")
				.concat("?s <http://www.w3.org/2000/01/rdf-schema#label> ?l . ")
				.concat("?l bif:contains '\"" + query + "\"' . ")
				.concat("FILTER (!regex(str(?s), '^http://dbpedia.org/resource/Category:')")
				.concat(" && !regex(str(?s), '^http://dbpedia.org/resource/List')")
				.concat(" && !regex(str(?s), '^http://sw.opencyc.org/')")
				.concat(" && lang(?l) = 'en'")
				.concat(" && !isLiteral(?someobj)")
				.concat(") . } ORDER BY DESC(?count) LIMIT " + nbResult);

		try {
			sb.append("http://dbpedia.org/sparql?default-graph-uri=")
					.append(URLEncoder.encode("http://dbpedia.org", "UTF-8"))
					.append("&query=")
					.append(URLEncoder.encode(queryString, "UTF-8"))
					.append("%0D%0A&debug=on&timeout=10000&format=")
					.append(URLEncoder.encode(
							"application/sparql-results+json", "UTF-8"))
					.append("&save=display&fname=");
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		URL url = null;
		try {
			url = new URL(sb.toString());
		} catch (Exception ex) {
		}

		System.out.println(url.toString());

		URLConnection connection = null;

		boolean isError = false;
		try {
			connection = url.openConnection();
		} catch (Exception ex) {
			isError = true;
			System.out.println(ex.getMessage());
		}

		if (!isError) {
			String line;
			StringBuilder builder = new StringBuilder();

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject jTotal = new JSONObject(builder.toString());
				// JSONArray listVar =
				// jTotal.getJSONObject("head").getJSONArray("vars");
				JSONArray listResults = jTotal.getJSONObject("results")
						.getJSONArray("bindings");

				for (int i = 0; i < listResults.length(); i++) {
					String s = listResults.getJSONObject(i).getJSONObject("s")
							.getString("value");
					// System.out.println(s);

					listURL[nbResult + i] = s;
				}

				// System.out.println("asdsad");
			} catch (Exception ex) {
			}

		}
	}

	private int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public String getClosestURL() {
		int closestResult = 999;
		String ans = "";
		try {
			getTopResults(10);
		} catch (Exception ex) {
		}
		getTopResults2(10);
		for (int i = 0; i < listURL.length; i++) {
			// for(String str: listURL){
			String str = listURL[i];
			if (str == null)
				continue;
			String[] strSplitted = str.split("/");
			int x = 999;
			try {
				x = computeLevenshteinDistance(URLDecoder.decode(
						strSplitted[strSplitted.length - 1], "UTF-8"));
			} catch (Exception ex) {
			}
			if (x < closestResult) {
				closestResult = x;
				ans = str;
			}
		}
		return ans;
	}

	private int computeLevenshteinDistance(String str2) {
		int[][] distance = new int[query.length() + 1][str2.length() + 1];

		for (int i = 0; i <= query.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= query.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((query.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[query.length()][str2.length()];
	}

}
