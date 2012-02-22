package com.cleargist.sintagesPareasScraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleargist.catalog.dao.CatalogDAO;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.entity.jaxb.Catalog;


public class Scraper {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final String BASE_URL = "http://www.sintagespareas.gr/sintages/";
	protected static final Locale locale = new Locale("el", "GR"); 
	public static String newline = System.getProperty("line.separator");
	protected static final int MAX_WAIT_TIME_BETWEEN_CRAWLS = 500;
	private String hrefPatternString = ".+ href=\"(.+)\".+";
	private Pattern hrefPattern = Pattern.compile(hrefPatternString);
	protected static String USER_AGENT = "ClearGist_bot";
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+\\.\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replaceAll("\\s+", " ");
		out = out.trim();
		
		return out;
	}
	
	private Catalog.Products.Product  scrapeDetailPage(String url) throws Exception {
		
		Catalog.Products.Product product = new Catalog.Products.Product();
		Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
		Elements openGraphTags = doc.getElementsByTag("meta");
		for (int i = 0; i < openGraphTags.size(); i ++) {
			List<Attribute> attributes = openGraphTags.get(i).attributes().asList();
			for (int att = 0; att < attributes.size(); att ++) {
				Attribute attribute = attributes.get(att);
				if (attribute.getKey().equals("property")) {
					if (attribute.getValue().equals("og:url")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.getKey().equals("content")) {
							product.setLink(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:image")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.getKey().equals("content")) {
							product.setImage(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:title")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.getKey().equals("content")) {
							product.setName(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:description")) {  
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.getKey().equals("content")) {
							product.setDescription(nextAttribute.getValue());
						}
					}
				}
			}
			
		}
		
		// Get canonical ID
		Elements elmnts = doc.select("link[rel=canonical]");
		if (elmnts != null && elmnts.size() > 0){
			Element el = elmnts.get(0);
			String elText = el.toString();
			Matcher canonicalMatcher = this.hrefPattern.matcher(elText);
			if (canonicalMatcher.matches()) {
				String canonical = canonicalMatcher.group(1);
				product.setUid(canonical);
			}
			else {
				product.setUid(url);
			}
		}
		else {
			product.setUid(url);
		}
		
		
		// Get ingredients
		Elements tmpElements = doc.getElementsByClass("rr_ingredients");
		if (tmpElements == null || tmpElements.size() == 0) {
			logger.info("No ingredients found for " + url);
		}
		else {
			Element element = tmpElements.get(0);
			StringBuffer sb = new StringBuffer();
			for (Element e : element.getElementsByTag("li")) {
				sb.append(" "); sb.append(e.text()); 
			}
//			String processedText = removeSpecialChars(sb.toString());
			String processedText = sb.toString();
			product.setDescription(processedText);
		}
		
		product.setCategory("FOOD");
		product.setPrice(BigDecimal.valueOf(0.0));
		product.setInstock("Y");
		
		return product;
	}
	
	/*
	 * Write the XML catalog in S3 under bucket cleargist file sintagesPareas.xml
	 */
	public void scrape(File recipesFile) {
		List<String> urls = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recipesFile)));
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String[] fields = lineStr.split(";");
				if (fields.length != 3 || fields[1].isEmpty()) {
					continue;
				}
				
				urls.add(BASE_URL + fields[1] + ".html");
			}
			br.close();
		}
		catch (IOException ex) {
    		logger.error("Could not read recipes list file " + recipesFile.getAbsolutePath());
    	}
		
		
		Catalog catalog = new Catalog();
		GregorianCalendar gc = new GregorianCalendar();
		Date date = new Date();
        gc.setTimeInMillis(date.getTime());
        DatatypeFactory df = null;
        try {
        	df = DatatypeFactory.newInstance();
        }
        catch (Exception ex) {
        	logger.error("Could not configure DatatypeFactory");
        	System.exit(-1);
        }
        
		catalog.setCreatedAt(df.newXMLGregorianCalendar(gc));
		catalog.setProducts(new Catalog.Products());
		Catalog.Products products = catalog.getProducts();
		List<Catalog.Products.Product> productList = products.getProduct();
		int cnt = 1;
		for (String url : urls) {
			logger.info(url);
			try {
				Catalog.Products.Product product = scrapeDetailPage(url);
				
				productList.add(product);
			}
			catch (Exception ex) {
	    		logger.warn("Could not read URL " + url);
	    	}
			
			try {
				Thread.sleep(MAX_WAIT_TIME_BETWEEN_CRAWLS);
			}
			catch (Exception ex) {
				logger.error("Could not sleep between requests");
				System.exit(-1);
			}
			
			if (cnt % 1000 == 0) {
				CatalogDAO dao = new CatalogDAOImpl();
				try {
					dao.marshallCatalog(catalog, "cleargist", "catalog.xsd", "cleargist", "sintagesPareas.xml", "sintagesPareas");
				}
				catch (Exception ex) {
					logger.error("Could not marshall catalog");
					System.exit(-1);
				}
			}
			
			cnt ++;
		}
		
		CatalogDAO dao = new CatalogDAOImpl();
		try {
			dao.marshallCatalog(catalog, "cleargist", "catalog.xsd", "cleargist", "sintagesPareas.xml", "sintagesPareas");
		}
		catch (Exception ex) {
			logger.error("Could not marshall catalog");
			System.exit(-1);
		}
	}
	
	
    public static void main( String[] args ) {
    	File recipesDirectory = new File("c:\\recs\\rr_recipes.csv");
    	
    	
        Scraper s = new Scraper();
        s.scrape(recipesDirectory);
        
    }
}
