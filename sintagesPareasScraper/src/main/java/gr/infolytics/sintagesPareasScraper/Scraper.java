package gr.infolytics.sintagesPareasScraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Scraper {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final String BASE_URL = "http://www.sintagespareas.gr/sintages/";
	public static String newline = System.getProperty("line.separator");
	protected static final int MAX_WAIT_TIME_BETWEEN_CRAWLS = 500;
	protected static String USER_AGENT = "Infolytics_bot";
	
	public void scrape(File recipesDirectory, File outRecipesText) {
		List<String> urls = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recipesDirectory)));
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String[] fields = lineStr.split(";");
				if (fields.length != 3 || fields[1].isEmpty()) {
					continue;
				}
				urls.add(fields[1]);
			}
			br.close();
		}
		catch (IOException ex) {
    		logger.error("Could not read file " + recipesDirectory.getAbsolutePath());
    	}
		
		BufferedWriter out = null;
		try {
			FileWriter fstream = new FileWriter(outRecipesText);
			out = new BufferedWriter(fstream);
		}
		catch (Exception ex) {
			logger.error("Could not write to " + outRecipesText.getAbsolutePath());
			System.exit(-1);
		}
		
		for (String url : urls) {
			logger.info(url);
			try {
				Document doc = Jsoup.connect(BASE_URL + url + ".html").userAgent(USER_AGENT).get();
				Elements tmpElements = doc.getElementsByClass("rr_ingredients");
				if (tmpElements == null || tmpElements.size() == 0) {
					continue;
				}
				Element element = tmpElements.get(0);
				StringBuffer sb = new StringBuffer();
				sb.append("\""); sb.append(url); sb.append("\";\"");
				for (Element e : element.getElementsByTag("li")) {
					sb.append(" "); sb.append(e.text()); 
				}
				sb.append("\"");
				sb.append(newline);
				
				try {
					out.write(sb.toString());
					out.flush();
				}
				catch (Exception ex) {
					logger.error("Could not write to " + outRecipesText.getAbsolutePath());
					System.exit(-1);
				}
			}
			catch (IOException ex) {
	    		logger.error("Could not read URL " + url);
	    	}
			
			try {
				Thread.sleep(MAX_WAIT_TIME_BETWEEN_CRAWLS);
			}
			catch (Exception ex) {
				logger.error("Could not sleep between requests");
				System.exit(-1);
			}
		}
	}
	
	
    public static void main( String[] args ) {
    	File recipesDirectory = new File("c:\\recs\\rr_recipes.csv");
    	File recipesTexts = new File("c:\\recs\\recipesTexts.txt");
    	
    	
        Scraper s = new Scraper();
        s.scrape(recipesDirectory, recipesTexts);
        
    }
}
