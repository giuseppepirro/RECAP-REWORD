package eu.gpirro.utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class EntitySuggestion {

	public static void main(String argv[]) {

		EntitySuggestion.getEntitySyggestion("Fritz_la");
	}

	private static void getEntitySyggestion(String string) {
		String q = "http://en.wikipedia.org/w/api.php?action=opensearch&search="
				+ string + "&limit=10&namespace=0&format=json";
		URL url;
		try {
			url = new URL(q.toString());

			URLConnection connection = null;

			connection = url.openConnection();

			String line;
			StringBuilder builder = new StringBuilder();

			BufferedReader reader = null;

			InputStream input_stream = null;

			input_stream = connection.getInputStream();

			reader = new BufferedReader(new InputStreamReader(input_stream));

			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String text = builder.toString();

			StringTokenizer st = new StringTokenizer(text, "[");

			/**
			 * METTERE tasto destro per far vedere le cose in WIKIPEDIA!!!!!
			 * 
			 * mettere dei pulsanti sulla barra per dire quale entita'
			 * aggiungere al grafo; senza riandare alla main GUI!!
			 */
			int i = 0;
			String temp = "";
			String good = "";
			String name = "";
			String uri = "";
			while (st.hasMoreTokens()) {
				if (i == 0 || i == 2)
					temp = st.nextToken();
				else if (i == 1) {
					name = st.nextToken();

				} else if (i == 3) {
					uri = st.nextToken();
				}

				i++;

			}

			name = name.replace("]]", "");
			name = name.replace("]", "");

			uri = uri.replace("]]", "");
			uri = uri.replace("]", "");

			// parse put in two data structures and add tot he hashtable;
			// when the user presses enter put the URI and LOAD the WIKI infobox
			// :)

			System.out.println(name);
			System.out.println(uri);

			Hashtable<String, String> name_uri = new Hashtable<String, String>();

			/**
			 * OBTAIN all the pairs (String, WIKI URI)
			 */

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
