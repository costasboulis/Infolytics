package com.cleargist.catalog.deals.scrape;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.cleargist.catalog.deals.entity.jaxb.DealType;

public class TitleCategoryTextClassifier extends TextClassifier {
	private static final Locale locale = new Locale("el", "GR"); 
	public static String newline = System.getProperty("line.separator"); 
	private Logger logger = Logger.getLogger(getClass());
	
	public HashMap<String, List<String>> getData(File dealsFile, File annotatedFile) {
		HashMap<String, List<String>> descriptions = new HashMap<String, List<String>>();
		
		// Unmarshall annotated deals
		List<DealType> annotatedDeals = null;
		try {
			annotatedDeals = unmarshall(annotatedFile);
    	}
    	catch (JAXBException ex) {
    		String errorMessage = "Failed to unmarshall from " + annotatedFile.getAbsolutePath();
    		logger.error(errorMessage);
    		System.exit(-1);
    	}
    	catch (IOException ex) {
    		logger.error("Failed reading from " + annotatedFile.getAbsolutePath());
    		System.exit(-1);
    	}
    	
    	// Unmarshall scraped deals
    	HashMap<String, DealType> scrapedDeals = new HashMap<String, DealType>();
    	try {
    		List<DealType> l = unmarshall(dealsFile);
    		for (DealType deal : l) {
    			scrapedDeals.put(deal.getDealId(), deal);
    		}
    	}
    	catch (JAXBException ex) {
    		String errorMessage = "Failed to unmarshall from " + dealsFile.getAbsolutePath();
    		logger.error(errorMessage);
    	}
    	catch (IOException ex) {
    		logger.error("Failed reading from " + dealsFile.getAbsolutePath());
    	}
		
		for (DealType annotatedDeal : annotatedDeals) {
			String dealID = annotatedDeal.getDealId();
			DealType scrapedDeal = scrapedDeals.get(dealID);
			
			if (scrapedDeal == null) {
				logger.warn("Could not find scraped deal " + dealID);
				continue;
			}
			
			String title = scrapedDeal.getDealTitle();
			if (title == null) {
				logger.warn("Could not get title for deal " + dealID);
				continue;
			}
			String merchantName = scrapedDeal.getBusinessName();
			String newMerchantName = merchantName.replaceAll(" ", "_");
			
			title = title.replaceAll(merchantName, newMerchantName);
			title = removeSpecialChars(title);
			
			if (title.isEmpty()) {
				logger.warn("Could not get text for deal " + dealID);
				continue;
			}
			
			String category = annotatedDeal.getCategory();
			if (category == null || category.isEmpty()) {
				logger.warn("Cannot get category for annotated deal " + dealID);
				continue;
			}
			
			List<String> categoryDescriptions = descriptions.get(category);
			if (categoryDescriptions == null) {
				categoryDescriptions = new LinkedList<String>();
				descriptions.put(category, categoryDescriptions);
			}
			
			categoryDescriptions.add(title);
		}
		
		return descriptions;
	}

	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+,\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("<b>", "");
		out = out.replaceAll("</b>", "");
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>€«®-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replaceAll("\\s+", " ");
		out = out.replaceAll("^number", "");
		out = out.replaceAll("εκπτωση number", "");
		out = out.replaceAll("αξιας number", "");
		out = out.trim();
		
		return out;
	}
	
}
