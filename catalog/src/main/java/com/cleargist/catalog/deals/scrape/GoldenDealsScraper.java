package com.cleargist.catalog.deals.scrape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;


import com.cleargist.catalog.dao.MyValidationEventHandler;
import com.cleargist.catalog.deals.entity.jaxb.AddressListType;
import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

public class GoldenDealsScraper {
	private static final Locale locale = new Locale("el", "GR"); 
	private Logger logger = Logger.getLogger(getClass());
	private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm";
	private SimpleDateFormat formatter;
	private GreekCouponRedemptionResolver couponRedemptionResolver;
	private GreekPhoneReservationResolver phoneReservationResolver;
	private GreekValidDatesResolver validDatesResolver;
	private GreekBlockerDatesResolver blockerDatesResolver;
	private GreekMaxCouponsPerPersonResolver maxCouponsPerPersonResolver;
	private GreekOnePersonCouponResolver onePersonCouponResolver;
	private GreekHasExtraDiscountsResolver hasExtraDiscountsResolver;
	private GreekHasOptionsResolver hasOptionsResolver;
	
	public GoldenDealsScraper() {
		this.formatter = new SimpleDateFormat(DATE_PATTERN);
		this.couponRedemptionResolver = new GreekCouponRedemptionResolver();
		this.phoneReservationResolver = new GreekPhoneReservationResolver();
		this.validDatesResolver = new GreekValidDatesResolver();
		this.blockerDatesResolver = new GreekBlockerDatesResolver();
		this.maxCouponsPerPersonResolver = new GreekMaxCouponsPerPersonResolver();
		this.onePersonCouponResolver = new GreekOnePersonCouponResolver();
		this.hasExtraDiscountsResolver = new GreekHasExtraDiscountsResolver();
		this.hasOptionsResolver = new GreekHasOptionsResolver();
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
	
	public static String html2text(String html) {
	    return Jsoup.parse(html).text();
	}
	
	public Collection scrape(List<String> urlList) {
		Collection collection = new Collection();
		Collection.Deals deals = new Collection.Deals();
		collection.setDeals(deals);
		List<DealType> dealsList = deals.getDeal();
		for (String url : urlList) {
			DealType deal = null;
			try {
				deal = scrape(url);
			}
			catch (Exception ex) {
				logger.warn("Could not scrape deal " + url);
				continue;
			}
			dealsList.add(deal);
			
			logger.info("Processed deal " + url);
		}
		return collection;
	}
	

	public DealType scrape(String url) throws Exception {
		DealType deal = new DealType();
		JSONObject json = readJsonFromUrl(url);
		
		String status = json.getString("status");
		if (!status.equals("OK")) {
			logger.warn("Could not get data from URL " + url);
			return deal;
		}
		
		JSONArray array = json.getJSONArray("resultData");
		JSONObject dealJson = null;
		String dealURLId = new URL(url).getPath().replaceAll("\\.js", "").replaceAll("/deals/json/detailed/", "");
		
		int length = array.length();
		for (int i = 0; i < length; i ++) {
			String dealURL = array.getJSONObject(i).getString("url");
			
			if (dealURL.equals(dealURLId)) {
				dealJson = array.getJSONObject(i);
				
				break;
			}
			
		}
		
		if (dealJson == null) {
			logger.warn("Could not locate deal for url " + url);
			return deal;
		}
		
		
//		for (String name : JSONObject.getNames(dealJson)) {
//			System.out.println(name);
//		}
		
		
		String merchantName = dealJson.getString("merchantName");
		
		String dueDateString = dealJson.getString("dueDate");
		Date dueDate = this.formatter.parse(dueDateString);
		String startDateString = dealJson.getString("startDate");
		Date startDate = this.formatter.parse(startDateString);
		String location = html2text(dealJson.getString("location"));
		String description = dealJson.getString("description");
		String descriptionShort = dealJson.getString("descriptionShort");
		String cityTag = dealJson.getString("cityTag");
		String title = dealJson.getString("title");
		float price = (float)dealJson.getDouble("price");
		Float initialPrice = (float)dealJson.getDouble("initialPrice");
		String nonDiscountString = null;
		try {
			nonDiscountString = dealJson.getString("nonDiscountString");
		}
		catch (Exception ex) {
			
		}
		JSONObject belongsToJson = dealJson.getJSONObject("belongsTo");
		List<String> categories = deal.getSiteCategories();
		for (String category : JSONObject.getNames(belongsToJson)) {
			if (category.startsWith("athens") || category.equals("all-categories")) {
				continue;
			}
			categories.add(category);
		}
		
		String[] descriptionParts = description.split("<h2>");
		String allTerms = html2text(descriptionParts[0]).toLowerCase(locale)
		.replaceAll("<\\\\/strong>", "")
		.replaceAll("<\\\\/li>", "")
		.replaceAll("<\\\\/ul>","")
		.replaceAll("<\\\\/b>", "")
		.replaceAll("\\[", "");
		String[] terms = allTerms.split("\\\\r\\\\n");
		
		
		deal.setSiteId("Golden Deals");
		deal.setSiteCity(cityTag);
		deal.setSiteCountry(locale.getCountry());
		deal.setDealId(dealURLId);
		deal.setDealPrice(new BigDecimal(price));
		deal.setInitialPrice(new BigDecimal(initialPrice.toString()));
		deal.setBusinessName(merchantName);
		AddressListType address = GreekAddressResolver.resolve(location.replaceAll("GoldenTip.+", ""));
		
		deal.setBusinessAddress(address);
		
		// Calendar conversions
		GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(startDate.getTime());
        DatatypeFactory df = DatatypeFactory.newInstance();
        
		deal.setCouponPurchaseStartingDate(df.newXMLGregorianCalendar(gc));
		
		gc.setTimeInMillis(dueDate.getTime());
		deal.setCouponPurchaseEndDate(df.newXMLGregorianCalendar(gc));
		
		deal.setDealDescription(description);
		deal.setDealTitle(title);
		
		// Process the date terms
		String dateTerms = null;
		for (String term : terms) {
			if (term.length() < 5) {
				continue;
			}
			dateTerms = term;
			break;
		}
		if (dateTerms == null) {
			logger.error("Could not determine date terms line");
			return deal;
		}
		
		
		List<Date> couponRedemptionDates = couponRedemptionResolver.resolve(dateTerms, dueDate.getYear());
		if (couponRedemptionDates.size() > 0) {
			if (couponRedemptionDates.get(0).getYear() == 1999) {
				couponRedemptionDates.set(0, dueDate);
			}
			GregorianCalendar gcTmp = new GregorianCalendar();
			gcTmp.setTimeInMillis(couponRedemptionDates.get(0).getTime());
			
			deal.setCouponRedemptionStartingDate(df.newXMLGregorianCalendar(gcTmp));
			
			if (couponRedemptionDates.size() > 1) {
				gcTmp.setTimeInMillis(couponRedemptionDates.get(1).getTime());
				
				deal.setCouponRedemptionEndDate(df.newXMLGregorianCalendar(gcTmp));
			}
		}
		
		// Process the valid dates
		List<String> validDates = deal.getValidDates();
		List<String> tmpValidDates = validDatesResolver.resolve(dateTerms);
		if (tmpValidDates != null && tmpValidDates.size() != 0) {
			validDates.addAll(tmpValidDates);
		}
		
		
		// Process the blocker dates
		boolean hasBlockerDates = blockerDatesResolver.resolve(dateTerms);
		deal.setHasBlockerDates(hasBlockerDates);
		
		
		// Process the phone reservation
		deal.setRequiresPhoneReservation(this.phoneReservationResolver.resolve(allTerms));
		
		
		// Process the coupons sold
		String urlCoupons = "http://dynamic.goldendeals.gr/deals/json/dynamic?city=" + deal.getSiteCity() + "&dealUrl=" + deal.getDealId();
		JSONObject jsonCoupons = readJsonFromUrl(urlCoupons);
		
		String statusCoupons = jsonCoupons.getString("status");
		if (!statusCoupons.equals("OK")) {
			logger.warn("Could not get data from URL " + urlCoupons);
		}
		else {
			JSONObject dealJsonCoupons = jsonCoupons.getJSONObject("resultData").getJSONObject(deal.getDealId());
			Integer numCoupons = dealJsonCoupons.getInt("sales");
			Integer tippedSales = dealJsonCoupons.getInt("tippedSales");
			
			deal.setNumberOfCouponsSold(new BigInteger(numCoupons.toString()));
			deal.setMinCouponsForActivation(new BigInteger(tippedSales.toString()));
			
			try {
				JSONArray variations = dealJsonCoupons.getJSONArray("variations");
				deal.setHasMultiplePrices(true);
			}
			catch (Exception ex) {
				deal.setHasMultiplePrices(false);
			}
			
			
		}
		
		// Process the max coupons per user
		deal.setMaxCouponsPerPerson(new BigInteger(Integer.toString(this.maxCouponsPerPersonResolver.resolve(allTerms))));
		
		// Process has more to pay
		deal.setRequiresMoreToPay(nonDiscountString == null || nonDiscountString.isEmpty() ? false : true);
		
		
		// Process one person coupon
		deal.setIsOnePersonCoupon(this.onePersonCouponResolver.resolve(allTerms));
		
		// Process has extra discounts
		deal.setHasExtraDiscounts(this.hasExtraDiscountsResolver.resolve(allTerms));
		
		// Process has options
		deal.setHasOptions(this.hasOptionsResolver.resolve(allTerms));
		
		return deal;
	}
	
	public void marshall(Collection collection, File XSDFile, File outputFile) throws Exception {
		
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
    		throw new Exception();
    	}
    	
    	try {
			marshaller.marshal(collection, new FileOutputStream(outputFile));
		}
		catch (JAXBException ex) {
			logger.error("Could not marshal the catalog");
			throw new Exception();
		}
		catch (FileNotFoundException ex2) {
			logger.error("Could not write to " + outputFile.getAbsolutePath());
			throw new Exception();
		}
	}
	
	public static void main(String[] argv) {
		String dealsFilenameString = "c:\\Users\\kboulis\\goldenDeals.txt";
//		String dealsFilenameString = "c:\\Users\\kboulis\\goldenDealsSmall.txt";
		GoldenDealsScraper scraper = new GoldenDealsScraper();
		
		File dealsFile = new File(dealsFilenameString);
		List<String> deals = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(dealsFile));
			String line = null;
			while (( line = br.readLine()) != null){
				deals.add(line + ".js");
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
			scraper.marshall(collection, new File("C:\\Users\\kboulis\\deals.xsd"), new File("C:\\Users\\kboulis\\Infolytics\\catalog\\goldenDeals.xml"));
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
	}
}
