package eu.gpirro.utilities;

import java.io.IOException;
import java.util.Vector;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetWikiPage {
	public static void main(String argv[]) {

		GetWikiPage.get("http://en.wikipedia.org/wiki/Fritz_Lang");
	}

	/**
	 * Return the HTML code and the address of the image!
	 * 
	 * @param address
	 * @return
	 */
	public static Vector<String> get(String address) 
	{
		Vector<String> final_res = new Vector<String>();
		Element table = null;
		Response res = null;
		try {
			res = Jsoup.connect(address).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		System.out.println("No wiki page for "+address);
		}

		try{
		
		String html = res.body();

		Document doc = Jsoup.parseBodyFragment(html);
		Element body = doc.body();

		Elements tables = body.getElementsByTag("table");

		for (Element tableq : tables) {

			// System.out.println(tableq.getElementsByAttribute("src").get(0));

			// System.out.println(tableq);
			if (tableq.className().startsWith("infobox")) {

				String t = tableq.toString();
				t = t.replace("//upload", "http://upload");
				final_res.add(t);
				
				
				Elements fig = tableq.select("img[src$=.jpg]");
				if(fig.size()>0)
				final_res.add("http:" + fig.get(0).attr("src"));

				return final_res;
			}
			return null;

		}
		}catch(Exception e)
		{
			
		}
		return null;

	}
}
