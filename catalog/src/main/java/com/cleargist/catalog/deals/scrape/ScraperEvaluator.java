package com.cleargist.catalog.deals.scrape;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

public class ScraperEvaluator {
	public static String newline = System.getProperty("line.separator"); 
	private Logger logger = Logger.getLogger(getClass());
	private HashMap<String, DealType> hypDeals;
	private HashMap<String, DealType> refDeals;
	
	private HashMap<String, DealType> readDeals(File dealsFile) {
		HashMap<String, DealType> hm = new HashMap<String, DealType>();
		Collection col = null;
		Unmarshaller unmarshaller = null;
    	try {
    		String contextPath = com.cleargist.catalog.deals.entity.jaxb.Collection.class.getCanonicalName().replaceAll("\\.Collection", "");
    		JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
    		unmarshaller = jaxbContext.createUnmarshaller();
    	}
    	catch (JAXBException ex) {
    		logger.error("Setting up unmarshalling failed");
    		System.exit(-1);
    	}
    	
		if (!dealsFile.exists()) {
            logger.error("Cannot find file " + dealsFile.getAbsolutePath());
            System.exit(-1);
        }
        if (!dealsFile.canRead()) {
            logger.error("Cannot read file " + dealsFile.getAbsolutePath());
            System.exit(-1);
        }
        if (dealsFile.isDirectory()) {
            logger.error("Expecting file but encountered directory " + dealsFile.getAbsolutePath());
            System.exit(-1);
        }
        
        try {
        	col = (Collection)unmarshaller.unmarshal(dealsFile);
        }
        catch (JAXBException ex) {
    		logger.error("Error while reading ads");
    		System.exit(-1);
    	}
        
        for (DealType deal : col.getDeals().getDeal()) {
        	String key = deal.getSiteId() + "_" + deal.getSiteCity() + "_" + deal.getDealId();
        	
        	hm.put(key, deal);
        }
        logger.info("Read data : " + dealsFile.getAbsolutePath());
        
		return hm;
	}
	
	private int[][] createConfusionMatrix(int[] hyp, int[] ref) {
		if (hyp.length != ref.length) {
			logger.error("Unequal REF & HYP vector sizes");
			return new int[2][2];
		}
		
		int[][] conf = new int[2][2];
		for (int i = 0; i < hyp.length; i ++) {
			conf[hyp[i]][ref[i]] ++;
		}
		
		return conf;
	}
	
	private String showConf(String name, int[][] conf) {
		StringBuffer sb = new StringBuffer();
		sb.append(name + newline);
		sb.append(conf[0][0]); sb.append(" ");  sb.append(conf[0][1]); sb.append(newline);
		sb.append(conf[1][0]); sb.append(" ");  sb.append(conf[1][1]); sb.append(newline);
		
		return sb.toString();
	}
	
	public static void convertRefDealsToSchema(File refDeals) throws Exception {
		
	}
	
	public void evaluate(File hypDealsFile, File refDealsFile) {
		
		hypDeals = readDeals(hypDealsFile);
		refDeals = readDeals(refDealsFile);
		
		if (hypDeals.size() != refDeals.size()) {
			logger.error("Number of HYP deals " + hypDeals.size() + " . Number of REF deals " + refDeals.size());
			System.exit(-1);
		}
		
		int length = refDeals.size();
		int[] hypHasBlockerDates = new int[length];
		int[] refHasBlockerDates = new int[length];
		int[] hypIsLuxuryBusiness = new int[length];
		int[] refIsLuxuryBusiness = new int[length];
		int[] hypHasMultiplePrices = new int[length];
		int[] refHasMultiplePrices = new int[length];
		int[] hypRequiresStoreVisit = new int[length];
		int[] refRequiresStoreVisit = new int[length];
		int[] hypRequiresMoreToPay = new int[length];
		int[] refRequiresMoreToPay = new int[length];
		int[] hypRequiresPhoneReservation = new int[length];
		int[] refRequiresPhoneReservation = new int[length];
		int[] hypIsOnePersonCoupon = new int[length];
		int[] refIsOnePersonCoupon = new int[length];
		int[] hypIsSingleVisitCoupon = new int[length];
		int[] refIsSingleVisitCoupon = new int[length];
		int[] hypHasExtraDiscounts = new int[length];
		int[] refHasExtraDiscounts = new int[length];
		int[] hypIsComboDeal = new int[length];
		int[] refIsComboDeal = new int[length];
		int[] hypHasOptions = new int[length];
		int[] refHasOptions = new int[length];
		
		int dealIndx = 0;
		for (Map.Entry<String, DealType> hypEntry : hypDeals.entrySet()) {
			String dealID = hypEntry.getKey();
			DealType hypDeal = hypEntry.getValue();
			
			DealType refDeal = refDeals.get(dealID);
			if (refDeal == null) {
				logger.error("Cannot find REF deal for " + dealID);
				System.exit(-1);
			}
			
			// Has Blocker Dates
			hypHasBlockerDates[dealIndx] = hypDeal.isHasBlockerDates() ? 1 : 0;
			refHasBlockerDates[dealIndx] = refDeal.isHasBlockerDates() ? 1 : 0;
			
			// Is Luxury Business
			hypIsLuxuryBusiness[dealIndx] = hypDeal.isIsLuxuryBusiness() ? 1 : 0;
			refIsLuxuryBusiness[dealIndx] = refDeal.isIsLuxuryBusiness() ? 1 : 0;
			
			// Has Multiple Prices
			hypHasMultiplePrices[dealIndx] = hypDeal.isHasMultiplePrices() ? 1 : 0;
			refHasMultiplePrices[dealIndx] = refDeal.isHasMultiplePrices() ? 1 : 0;
			
			// Requires store visit
			hypRequiresStoreVisit[dealIndx] = hypDeal.isRequiresStoreVisit() ? 1 : 0;
			refRequiresStoreVisit[dealIndx] = refDeal.isRequiresStoreVisit() ? 1 : 0;
			
			// Requires More to Pay
			hypRequiresMoreToPay[dealIndx] = hypDeal.isRequiresMoreToPay() ? 1 : 0;
			refRequiresMoreToPay[dealIndx] = refDeal.isRequiresMoreToPay() ? 1 : 0;
			
			// Requires Phone Reservation
			hypRequiresPhoneReservation[dealIndx] = hypDeal.isRequiresPhoneReservation() ? 1 : 0;
			refRequiresPhoneReservation[dealIndx] = refDeal.isRequiresPhoneReservation() ? 1 : 0;
			
			// Is One Person Coupon
			hypIsOnePersonCoupon[dealIndx] = hypDeal.isIsOnePersonCoupon() ? 1 : 0;
			refIsOnePersonCoupon[dealIndx] = refDeal.isIsOnePersonCoupon() ? 1 : 0;
			
			// Is Single Visit Coupon
			hypIsSingleVisitCoupon[dealIndx] = hypDeal.isIsSingleVisitCoupon() ? 1 : 0;
			refIsSingleVisitCoupon[dealIndx] = refDeal.isIsSingleVisitCoupon() ? 1 : 0;
			
			// Has Extra Discounts
			hypHasExtraDiscounts[dealIndx] = hypDeal.isHasExtraDiscounts() ? 1 : 0;
			refHasExtraDiscounts[dealIndx] = refDeal.isHasExtraDiscounts() ? 1 : 0;
			
			// Is Combo Deal
			hypIsComboDeal[dealIndx] = hypDeal.isIsComboDeal() ? 1 : 0;
			refIsComboDeal[dealIndx] = refDeal.isIsComboDeal() ? 1 : 0;
			
			// Has Options
			hypHasOptions[dealIndx] = hypDeal.isHasOptions() ? 1 : 0;
			refHasOptions[dealIndx] = refDeal.isHasOptions() ? 1 : 0;
			
			
			dealIndx ++;
		}
		
		// Has Blocker Dates Confusion Matrix
		int[][] conf = createConfusionMatrix(hypHasBlockerDates, refHasBlockerDates);
		System.out.println(showConf("HasBlockerDates", conf));
		
		// Is Luxury Business Confusion Matrix
		conf = createConfusionMatrix(hypIsLuxuryBusiness, refIsLuxuryBusiness);
		System.out.println(showConf("IsLuxuryBusiness", conf));
		
		// Has Multiple Prices Confusion Matrix
		conf = createConfusionMatrix(hypHasMultiplePrices, refHasMultiplePrices);
		System.out.println(showConf("HasMultiplePrices", conf));
		
		// Requires store visit Confusion Matrix
		conf = createConfusionMatrix(hypRequiresStoreVisit, refRequiresStoreVisit);
		System.out.println(showConf("RequiresStoreVisit", conf));
		
		// Requires More to Pay Confusion Matrix
		conf = createConfusionMatrix(hypRequiresMoreToPay, refRequiresMoreToPay);
		System.out.println(showConf("RequiresMoreToPay", conf));
		
		// Requires Phone Reservation Confusion Matrix
		conf = createConfusionMatrix(hypRequiresPhoneReservation, refRequiresPhoneReservation);
		System.out.println(showConf("RequiresPhoneReservation", conf));
		
		// Is One Person Coupon Confusion Matrix
		conf = createConfusionMatrix(hypIsOnePersonCoupon, refIsOnePersonCoupon);
		System.out.println(showConf("IsOnePersonCoupon", conf));
		
		// Is Single Visit Coupon Confusion Matrix
		conf = createConfusionMatrix(hypIsSingleVisitCoupon, refIsSingleVisitCoupon);
		System.out.println(showConf("IsSingleVisitCoupon", conf));
		
		// Has Extra Discounts Confusion Matrix
		conf = createConfusionMatrix(hypHasExtraDiscounts, refHasExtraDiscounts);
		System.out.println(showConf("HasExtraDiscounts", conf));
		
		// Is Combo Deal Confusion Matrix
		conf = createConfusionMatrix(hypIsComboDeal, refIsComboDeal);
		System.out.println(showConf("IsComboDeal", conf));
		
		// Has Options Confusion Matrix
		conf = createConfusionMatrix(hypHasOptions, refHasOptions);
		System.out.println(showConf("HasOptions", conf));
		
	}
	
	public static void main(String[] argv) {
		String hypDealsFilename = "";
		String refDealsFilename = "";
		ScraperEvaluator eval = new ScraperEvaluator();
		
		eval.evaluate(new File(hypDealsFilename), new File(refDealsFilename));
	}

}
