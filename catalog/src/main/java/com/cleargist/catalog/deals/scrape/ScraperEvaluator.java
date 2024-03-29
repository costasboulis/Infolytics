package com.cleargist.catalog.deals.scrape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import com.cleargist.catalog.dao.MyValidationEventHandler;
import com.cleargist.catalog.deals.entity.jaxb.AddressListType;
import com.cleargist.catalog.deals.entity.jaxb.AddressType;
import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

public class ScraperEvaluator {
	public static String newline = System.getProperty("line.separator"); 
	private Locale locale = new Locale("el", "GR");
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
	
	private String zeroPadDate(String date) {
		String[] tmp = date.trim().split("/");
		int day = Integer.parseInt(tmp[0]);
		int month = Integer.parseInt(tmp[1]);
		String[] moreFlds = tmp[2].split(" ");
		String yearString = moreFlds[0];
		int year = Integer.parseInt(yearString);
		if (year < 50) {
			year += 2000;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%02d", day)); sb.append("/");
		sb.append(String.format("%02d", month)); sb.append("/");
		sb.append(year);
		
		tmp = date.trim().split(" ");
		sb.append(" "); sb.append(tmp[1]);
		
		return sb.toString();
	}
	
	public void convertRefDealsToSchema(File refDeals, File XSDFile, File outRefXMLDeals) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
		NumberFormat doubleNumberFormat = NumberFormat.getNumberInstance(locale); 
		doubleNumberFormat.setMaximumFractionDigits(2);
		doubleNumberFormat.setMinimumFractionDigits(0);
		
		try {
			Collection collection = new Collection();
			collection.setDeals(new Collection.Deals());
			List<DealType> deals = collection.getDeals().getDeal();
			
    		BufferedReader reader = new BufferedReader(new FileReader(refDeals));
    		String line = reader.readLine();
			HashMap<String, Integer> fields = new HashMap<String, Integer>();
			String[] flds = line.split(";");
			int cnt = 0;
			for (String s : flds) {
				fields.put(s, cnt);
				
				cnt ++;
			}
			while (( line = reader.readLine()) != null){
				flds = line.split(";");
				DealType deal = new DealType();
				String dealID = flds[fields.get("URL")];
				if (!dealID.startsWith("http")) {
					continue;
				}
				deal.setDealURL(dealID);
				
				dealID = dealID.replaceAll("http://www.goldendeals.gr/deals/", "");
				deal.setDealId(dealID);
				
				String merchantName = flds[fields.get("NameOfBusiness")].trim();
				deal.setBusinessName(merchantName);
				
				String siteID = flds[fields.get("Provider")];
				if (siteID.equals("GoldenDeals")) {
					deal.setSiteId("Golden Deals");
				}
				
				String city = flds[fields.get("Location")];
				deal.setSiteCity(city.toLowerCase());
				deal.setSiteCountry("GR");
				
				String category = flds[fields.get("GrouponCategory")];
				deal.setCategory(category.trim());
				
				String subCategory = flds[fields.get("GrouponSubCategory")];
				deal.setSubCategory(subCategory.trim());
				
				deal.setMinCouponsForActivation(new BigInteger(flds[fields.get("MinCoupons")]));
				
				deal.setMaxCouponsPerPerson(new BigInteger(flds[fields.get("MaxCouponsPerPerson")]));
				
				deal.setNumberOfCouponsSold((new BigInteger(flds[fields.get("NumberOfCoupons")])));
				
				try {
		        	double p = doubleNumberFormat.parse(flds[fields.get("InitialPrice")]).doubleValue();
		        	deal.setInitialPrice((new BigDecimal(Double.toString(p))));
		        }
		        catch (ParseException ex) {
		        	logger.error("Cannot parse Initial Price in deal " + dealID);
		        	System.exit(-1);
		        }
				
		        try {
		        	double p = doubleNumberFormat.parse(flds[fields.get("PriceAfterDiscount")]).doubleValue();
		        	deal.setDealPrice((new BigDecimal(Double.toString(p))));
		        }
		        catch (ParseException ex) {
		        	logger.error("Cannot parse Deal Price in deal " + dealID);
		        	System.exit(-1);
		        }
				
				
				String date = flds[fields.get("FromCouponDate")];
				String paddedDate = zeroPadDate(date);
				Date fromDate = formatter.parse(paddedDate);
				GregorianCalendar gc = new GregorianCalendar();
		        gc.setTimeInMillis(fromDate.getTime());
		        DatatypeFactory df = DatatypeFactory.newInstance();
				deal.setCouponPurchaseStartingDate(df.newXMLGregorianCalendar(gc));
				
				
				date = flds[fields.get("ToCouponDate")];
				Date toDate = formatter.parse(zeroPadDate(date));
		        gc.setTimeInMillis(toDate.getTime());
				deal.setCouponPurchaseEndDate(df.newXMLGregorianCalendar(gc));
				
				
				date = flds[fields.get("DealStartDate")];
				fromDate = formatter2.parse(GreekCouponRedemptionResolver.zeroPadDate(date));
		        gc.setTimeInMillis(fromDate.getTime());
				deal.setCouponRedemptionStartingDate(df.newXMLGregorianCalendar(gc));
				
				
				date = flds[fields.get("DealEndDate")];
				toDate = formatter2.parse(GreekCouponRedemptionResolver.zeroPadDate(date));
		        gc.setTimeInMillis(toDate.getTime());
				deal.setCouponRedemptionEndDate(df.newXMLGregorianCalendar(gc));
				
				
				AddressType address = new AddressType();
				address.setCity("Unknown");
				address.setGeographicalArea("Unknown");
				AddressListType addressList = new AddressListType();
				List<AddressType> adrList = addressList.getAddress();
				adrList.add(address);
				deal.setBusinessAddress(addressList);
				
				boolean requiresStoreVisit = flds[fields.get("RequiresPhysicalVisit")].equals("Yes") ? true : false;
				deal.setRequiresStoreVisit(requiresStoreVisit);
				
				
//				boolean hasMultipleStores = flds[fields.get("MultipleStores")].equals("Yes") ? true : false;
				
				boolean requiresPhoneReservation = flds[fields.get("PhoneReservation")].equals("Yes") ? true : false;
				deal.setRequiresPhoneReservation(requiresPhoneReservation);
						
				boolean hasBlockerDates = flds[fields.get("ValidWithoutTimeExceptions")].equals("Yes") ? false : true;
				deal.setHasBlockerDates(hasBlockerDates);
				
				boolean isOnePersonCoupon = flds[fields.get("OnePersonCoupon")].equals("Yes") ? true : false;
				deal.setIsOnePersonCoupon(isOnePersonCoupon);
				
				boolean hasExtraDiscounts = flds[fields.get("ExtraDiscounts")].equals("Yes") ? true : false;
				deal.setHasExtraDiscounts(hasExtraDiscounts);
				
				boolean isComboDeal = flds[fields.get("ComboDeal")].equals("Yes") ? true : false;
				deal.setIsComboDeal(isComboDeal);
				
				boolean hasOptions = flds[fields.get("Has Options")].equals("Yes") ? true : false;
				deal.setHasOptions(hasOptions);
				
				boolean hasMultiplePrices = flds[fields.get("MultiplePrices")].equals("Yes") ? true : false;
				deal.setHasMultiplePrices(hasMultiplePrices);
				
				boolean hasMoreToPay = flds[fields.get("MoreToPay")].equals("Yes") ? true : false;
				deal.setRequiresMoreToPay(hasMoreToPay);
				
				boolean isValidForEveryWeekday = flds[fields.get("ValidForEveryWeekday")].equals("Yes") ? true : false;
				List<String> validDays = deal.getValidDates();
				if (isValidForEveryWeekday) {
					validDays.add("Monday"); validDays.add("Tuesday"); validDays.add("Wednesday");
					validDays.add("Thursday"); validDays.add("Friday");
				}
				boolean isValidForSaturday = flds[fields.get("ValidForSaturdays")].equals("Yes") ? true : false;
				if (isValidForSaturday) {
					validDays.add("Saturday");
				}
				
				boolean isValidForSunday = flds[fields.get("ValidForSundays")].equals("Yes") ? true : false;
				if (isValidForSunday) {
					validDays.add("Sunday");
				}
				
				
				deals.add(deal);
			}
    		reader.close();
    		
    		// Marshall the reference deals
    		Marshaller marshaller = null;
        	try {
        		String contextPath = com.cleargist.catalog.deals.entity.jaxb.Collection.class.getCanonicalName().replaceAll("\\.Collection", "");
        		JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
        		marshaller = jaxbContext.createMarshaller();
        		SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        		Schema schema = null;
        		try {
        			schema = sf.newSchema(XSDFile);
        		}
        		catch (Exception e){
        			logger.warn("Cannot create schema, check schema location " + XSDFile.getAbsolutePath());
        			System.exit(-1);
        		}
        		marshaller.setSchema(schema);
        		marshaller.setEventHandler(new MyValidationEventHandler());
        		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        	}
        	catch (JAXBException ex) {
        		logger.error("Setting up marshalling failed");
        		System.exit(-1);
        	}
        	
        	try {
    			marshaller.marshal(collection, new FileOutputStream(outRefXMLDeals));
    		}
    		catch (JAXBException ex) {
    			logger.error("Could not marshal the catalog");
    			System.exit(-1);
    		}
    		catch (FileNotFoundException ex2) {
    			logger.error("Could not write to " + outRefXMLDeals.getAbsolutePath());
    			System.exit(-1);
    		}
		}
		catch (Exception ex) {
			logger.error("Cannot read from file " + refDeals.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	private float calculateAccuracy(List<String> hyp, List<String> ref) {
		float acc = 0.0f;
		for (int i = 0 ; i < hyp.size(); i ++) {
			if (hyp.get(i).equals(ref.get(i))) {
				acc += 1.0f;
			}
		}
		
		return acc / (float)ref.size();
	}
	
	public void evaluate(File hypDealsFile, File refDealsFile) {
		
		hypDeals = readDeals(hypDealsFile);
		refDeals = readDeals(refDealsFile);
		
		if (hypDeals.size() > refDeals.size()) {
			logger.error("Number of HYP deals " + hypDeals.size() + " . Number of REF deals " + refDeals.size());
			System.exit(-1);
		}
		
		int length = hypDeals.size();
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
		List<String> hypCouponRedemption = new ArrayList<String>();
		List<String> refCouponRedemption = new ArrayList<String>();
		
		int dealIndx = 0;
		for (Map.Entry<String, DealType> hypEntry : hypDeals.entrySet()) {
			String dealID = hypEntry.getKey();
			DealType hypDeal = hypEntry.getValue();
			
			DealType refDeal = refDeals.get(dealID);
			if (refDeal == null) {
				logger.error("Cannot find REF deal for " + dealID);
				System.exit(-1);
			}
			
			// Coupon Redemption dates
			String couponStartDate = hypDeal.getCouponRedemptionStartingDate() == null ? "NULL" : hypDeal.getCouponRedemptionStartingDate().toString();
			String couponEndDate = hypDeal.getCouponRedemptionEndDate() == null ? "NULL" : hypDeal.getCouponRedemptionEndDate().toString();
			String couponRedemptionHypDates = couponStartDate + "_" + couponEndDate;
			hypCouponRedemption.add(couponRedemptionHypDates);
			
			String couponStartRefDate = refDeal.getCouponRedemptionStartingDate() == null ? "NULL" : refDeal.getCouponRedemptionStartingDate().toString();
			String couponEndRefDate = refDeal.getCouponRedemptionEndDate() == null ? "NULL" : refDeal.getCouponRedemptionEndDate().toString();
			String couponRedemptionRefDates = couponStartRefDate + "_" + couponEndRefDate;
			refCouponRedemption.add(couponRedemptionRefDates);
			
			
			// Has Blocker Dates
			hypHasBlockerDates[dealIndx] = hypDeal.isHasBlockerDates() ? 1 : 0;
			refHasBlockerDates[dealIndx] = refDeal.isHasBlockerDates() ? 1 : 0;
			
			// Is Luxury Business
//			hypIsLuxuryBusiness[dealIndx] = hypDeal.isIsLuxuryBusiness() ? 1 : 0;
//			refIsLuxuryBusiness[dealIndx] = refDeal.isIsLuxuryBusiness() ? 1 : 0;
			
			// Has Multiple Prices
			hypHasMultiplePrices[dealIndx] = hypDeal.isHasMultiplePrices() ? 1 : 0;
			refHasMultiplePrices[dealIndx] = refDeal.isHasMultiplePrices() ? 1 : 0;
			
			// Requires store visit
//			hypRequiresStoreVisit[dealIndx] = hypDeal.isRequiresStoreVisit() ? 1 : 0;
//			refRequiresStoreVisit[dealIndx] = refDeal.isRequiresStoreVisit() ? 1 : 0;
			
			// Requires More to Pay
			hypRequiresMoreToPay[dealIndx] = hypDeal.isRequiresMoreToPay() ? 1 : 0;
			refRequiresMoreToPay[dealIndx] = refDeal.isRequiresMoreToPay() ? 1 : 0;
			
			// Requires Phone Reservation
			hypRequiresPhoneReservation[dealIndx] = hypDeal.isRequiresPhoneReservation() ? 1 : 0;
			refRequiresPhoneReservation[dealIndx] = refDeal.isRequiresPhoneReservation() ? 1 : 0;
			if (hypRequiresPhoneReservation[dealIndx] == 1 && refRequiresPhoneReservation[dealIndx] == 0) {
				logger.info(dealID);
			}
			
			// Is One Person Coupon
			hypIsOnePersonCoupon[dealIndx] = hypDeal.isIsOnePersonCoupon() ? 1 : 0;
			refIsOnePersonCoupon[dealIndx] = refDeal.isIsOnePersonCoupon() ? 1 : 0;
//			if (hypIsOnePersonCoupon[dealIndx] == 1 && refIsOnePersonCoupon[dealIndx] == 0) {
//				logger.info(dealID);
//			}
			
			// Is Single Visit Coupon
//			hypIsSingleVisitCoupon[dealIndx] = hypDeal.isIsSingleVisitCoupon() ? 1 : 0;
//			refIsSingleVisitCoupon[dealIndx] = refDeal.isIsSingleVisitCoupon() ? 1 : 0;
			
			// Has Extra Discounts
			hypHasExtraDiscounts[dealIndx] = hypDeal.isHasExtraDiscounts() ? 1 : 0;
			refHasExtraDiscounts[dealIndx] = refDeal.isHasExtraDiscounts() ? 1 : 0;
//			if (hypHasExtraDiscounts[dealIndx] == 0 && refHasExtraDiscounts[dealIndx] == 1) {
//				logger.info(dealID);
//			}
			
			// Is Combo Deal
//			hypIsComboDeal[dealIndx] = hypDeal.isIsComboDeal() ? 1 : 0;
//			refIsComboDeal[dealIndx] = refDeal.isIsComboDeal() ? 1 : 0;
			
			// Has Options
			hypHasOptions[dealIndx] = hypDeal.isHasOptions() ? 1 : 0;
			refHasOptions[dealIndx] = refDeal.isHasOptions() ? 1 : 0;
			
			
			dealIndx ++;
		}
		
		// Coupon redemption dates
		System.out.println("Coupon Redemption Dates Accuracy : " + calculateAccuracy(hypCouponRedemption, refCouponRedemption));
		
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
		String hypDealsFilename = "C:\\recs\\GoldenDealsScraped.xml";
		String refDealsFilename = "C:\\recs\\AllDealsReference.xml";
		ScraperEvaluator eval = new ScraperEvaluator();
		
//		eval.convertRefDealsToSchema(new File("C:\\recs\\All_Deals.csv"), new File("C:\\Users\\kboulis\\deals.xsd"), new File(refDealsFilename));
		
		eval.evaluate(new File(hypDealsFilename), new File(refDealsFilename));
	}

}
