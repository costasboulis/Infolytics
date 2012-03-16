package com.cleargist.catalog.deals.scrape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

public class GrouponScraper extends Scraper {
	private String cityPatternString = ".*deals/([\\w-]+)/.*";
	private Pattern cityPattern;
	
	public GrouponScraper() {
		this.cityPattern = Pattern.compile(cityPatternString);
	}
	
	public DealType scrape(String url) throws Exception {
		DealType deal = new DealType();
		Document doc = Jsoup.connect(url).userAgent("ClearGist_bot").get();
		
		Element merchantContact = doc.select("[class=merchantContact]").get(0);
		String merchantName = merchantContact.getElementsByAttributeValue("class", "subHeadline").get(0).text();
		
		Element titleElement = doc.select("[class=title]").get(0);
		String title = titleElement.text();
		
		Element termsElementA = doc.select("[class=viewHalfWidthSize]").get(0);
		String termsA = termsElementA.text();
		Element termsElementB = doc.select("[class=viewHalfWidthSize]").get(1);
		String termsB = termsElementB.text();
		
		Element merchantDescElement = doc.select("[class=contentBoxNormalLeft]").get(0);
		String merchantDescription = merchantDescElement.text();
		
		deal.setMerchantDescription(merchantDescription);
		deal.setTerms(termsA + termsB);
		deal.setSiteCity(getCity(url));
		deal.setDealTitle(title);
		deal.setDealId(url);
		deal.setDealURL(url);
		deal.setSiteId("Groupon");
		deal.setSiteCountry("GR");
		deal.setBusinessName(merchantName == null || merchantName.isEmpty() ? "Unknown" : merchantName);
		
		return deal;
	}

	private String getCity(String text) {
		Matcher cityMatcher = this.cityPattern.matcher(text);
		if (cityMatcher.matches()) {
			String city = cityMatcher.group(1);
			return city == null || city.isEmpty() ? "Unknown" : city;
		}
		
		return "Unknown";
	}
	public static void main(String[] argv) {
		String dealsFilenameString = "c:\\recs\\GrouponDealsList.csv";
//		String dealsFilenameString = "c:\\recs\\GrouponDealsListSmall.csv";
		GrouponScraper scraper = new GrouponScraper();

		File dealsFile = new File(dealsFilenameString);
		List<String> deals = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(dealsFile));
			String line = null;
			while (( line = br.readLine()) != null){
				deals.add(line);
			}
			br.close();
		}
		catch (Exception ex) {
			System.err.println("Cannot load deals list");
			System.exit(-1);
		}

		Collection collection = null;
		try {
			collection = scraper.scrape(deals);
		}
		catch (Exception ex) {
			System.err.println("Cannot scrape url");
			System.exit(-1);
		}

		try {
			scraper.marshall(collection, new File("C:\\Users\\kboulis\\deals.xsd"), new File("C:\\recs\\GrouponDealsScraped.xml"));
		}
		catch (Exception ex) {
			System.exit(-1);
		}

	}
}
