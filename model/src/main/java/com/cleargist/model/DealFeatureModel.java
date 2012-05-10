package com.cleargist.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cleargist.catalog.dao.CatalogDAOImpl;
import com.cleargist.catalog.deals.entity.jaxb.AddressType;
import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;
import com.cleargist.catalog.deals.scrape.GoldenDealsScraper;
import com.cleargist.catalog.deals.scrape.Scraper;
import com.cleargist.catalog.entity.jaxb.Catalog;
import com.cleargist.catalog.entity.jaxb.Catalog.Products.Product;
import com.cleargist.profile.Profile;
import com.cleargist.profile.ProfileDAO;
import com.cleargist.profile.ProfileDAOImplS3;

//TODO: Bug when there are multiple deals bought in the same day. A negative deal may be selected that is really positive


public class DealFeatureModel extends BaseModel {
	private Scraper scraper = new GoldenDealsScraper();
	private Logger logger = Logger.getLogger(getClass());
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	private long dealsStartingTime;
	private HashMap<String, Instance> dealInstances;
	private HashMap<String, Integer> dealFirstDay; 
	private ProfileDAO profileAccessor;
	private Random rand;
	private List<List<DealType>> deals;  // Deals arranged according to day of first launch
	private String xsdBucket;            // S3 bucket where deals schema is stored
	private String xsdKey;               // S3 key where deals schema is stored
	private String bucket;               // S3 bucket where the deals after Information Extraction are stored
	private String key;                  // S3 key where the deals after Information Extraction are stored
	private Instances instances;
	private String PRICE = "Price";
	private String DISCOUNT = "Discount";
	private String NUM_DAYS_TO_PURCHASE = "NumDaysToPurchase";
	private String NUM_DAYS_TO_REDEEM = "NumDaysToRedeem";
	private String NUM_DAYS_UNTIL_START_OF_REDEEM = "NumDaysUntilStartOfRedeem";
	private String CAN_BE_REDEEMED_IN_EVERY_WEEKDAY = "CanBeRedeemedInEveryWeekday";
	private String CAN_BE_REDEEMED_IN_WEEKENDS = "CanBeRedeemedInWeekends";
	private String HAS_BLOCKER_DATES = "HasBlockerDates";
	private String HAS_MULTIPLE_PRICES = "HasMultiplePrices";
	private String REQUIRES_MORE_TO_PAY = "RequiresMoreToPay";
	private String REQUIRES_PHONE_RESERVATION = "RequiresPhoneReservation";
	private String IS_ONE_PERSON_COUPON = "IsOnePersonCoupon";
	private String HAS_EXTRA_DISCOUNTS = "HasExtraDiscounts";
	private String HAS_MULTIPLE_LOCATIONS = "HasMultipleLocations";
	private String[] cities = {"Athens", "Thessaloniki"};
	private String[] locations = {"Piraeus and neighboring suburbs", "Athens Center", "Athens South and Center Suburbs", 
								  "Athens North Suburbs", "Athens West Suburbs", "Rest of Attica", "Cyclades", 
								  "Dodekanissa", "Ionian islands", "Rest of Greece", "Crete", "Halkidiki",
								  "Foreign destinations", "Unknown", "Thessaloniki"};
	private String[] primaryCategories = {"ArtsandEntertainment", "Automotive", "BeautySpas",
										  "Education", "FoodDrink", "HealthFitness",
										  "HomeServices", "Nightlife", "Pets", "ProfessionalServices", 
										  "Restaurants", "Shopping", "Travel"};
	
	
	public DealFeatureModel() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getTimeZone("GR"));
		calendar.set(2010, 4, 26);
		
		dealsStartingTime = calendar.getTimeInMillis();
		
		this.instances = initInstances();
		
		this.rand = new Random();
		
		this.profileAccessor = new ProfileDAOImplS3();
	}
	
	public void setXSDBucket(String xsdBucket) {
		this.xsdBucket = xsdBucket;
	}
	
	public void setXSDKey(String xsdKey) {
		this.xsdKey = xsdKey;
	}
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	private List<AttributeObject> getClusterMemberships(String userID) throws Exception {
		List<AttributeObject> clusterMemberships = new ArrayList<AttributeObject>();
		return clusterMemberships;
	}
	
	public void createModel(String tenantID) throws AmazonServiceException, AmazonClientException, Exception {
		
		// Retrieve any new deals from last update, arrange deals according to day of first launch and extract Instance for each deal
		updateExtractedDeals(this.xsdBucket, this.xsdKey, this.bucket, this.key);
		
		// Create classifier training data for each cluster
		List<Instances> clusterInstances = new ArrayList<Instances>();
		this.profileAccessor.initSequentialProfileRead(tenantID);
		Profile profile = null;
		while ((profile = this.profileAccessor.getNextProfile()) != null) {
			// Find the clusters this user belongs to
			String userID = profile.getUserID();
			List<AttributeObject> clusterMemberships = getClusterMemberships(userID);
			
			List<List<Instance>> insts = getInstancesFromProfile(profile);
			List<Instance> positiveInstances = insts.get(0);
			
			
			List<Instance> negativeInstances = insts.get(1);
		}
		this.profileAccessor.closeSequentialProfileRead();
		
		// Go through each profile, retrieve prob(cluster | user)
	}
	
	/*
	 * Writes the new structured deals in S3 and returns all deals according to the day of launch
	 */
	private void updateExtractedDeals(String xsdBucket, String xsdKey, String bucket, String key) throws Exception {
		// Load already extracted deals
		boolean doesStructuredDealsFileExist = isValidFile(bucket, key);
		HashMap<String, Calendar> extractedDeals = new HashMap<String, Calendar>();
		List<DealType> extractedDealsList = new ArrayList<DealType>();
		Collection collection = null;
		if (doesStructuredDealsFileExist) {
			collection = scraper.unmarshall(bucket, key);
			extractedDealsList = collection.getDeals().getDeal();
			for (DealType deal : extractedDealsList) {
				String dealID = deal.getDealId();
				Calendar lastUpdate = new GregorianCalendar();
				
				extractedDeals.put(dealID, lastUpdate);
			}
		}
		else {
			collection = new Collection();
			Collection.Deals deals = new Collection.Deals();
			collection.setDeals(deals);
			extractedDealsList = deals.getDeal();
		}
		
		// Now scrape and add the new deals
		Catalog catalog = new Catalog();
		boolean foundNewDeal = false;
		for (Product product : catalog.getProducts().getProduct()) {
			String dealID = product.getUid();
			
			Calendar lastUpdate = extractedDeals.get(dealID);
			if (lastUpdate == null) {
				// Scrape deal
				DealType newDeal = scraper.scrape(product.getLink());
				extractedDealsList.add(newDeal);
				
				foundNewDeal = true;
			}
		}
		if (foundNewDeal) {
			scraper.marshall(collection, xsdBucket, xsdKey, bucket, key);
		}
		
		dealFirstDay = new HashMap<String, Integer>();
		this.deals = new ArrayList<List<DealType>>();
		for (DealType deal : extractedDealsList) {
			int counter = getDayCounter(deal);
			List<DealType> d = deals.get(counter);
			if (d == null) {
				d = new ArrayList<DealType>();
				deals.set(counter, d);
			}
			d.add(deal);
			
			dealInstances.put(deal.getDealId(), getInstance(deal));
			
			dealFirstDay.put(deal.getDealId(), getDayCounter(deal));
		}
		
	}
	
	private int getDayCounter(DealType deal) {
		Calendar startingDate = deal.getCouponPurchaseStartingDate().toGregorianCalendar();
		int numDays = Math.round((float)(startingDate.getTimeInMillis() - this.dealsStartingTime) / (1000.0f * 60.0f * 60.0f * 24.0f));
		
		return numDays;
	}
	
	/*
	 * Price : Float
	 * Discount : Float
	 * Number of days to purchase : Float
	 * Number of days to redeem : Float
	 * Number of days until redemption is on : Float
	 * Can be redeemed in every weekday : Binary
	 * Can be redeemed in weekends : Binary
	 * Can be redeemed in multiple places : Binary
	 * Has blocker dates : Binary
	 * Has multiple prices : Binary
	 * Requires more to pay : Binary
	 * Requires phone reservation : Binary 
	 * Is one-person coupon : Binary
	 * Has extra discounts : Binary
	 * Location of redeemed places : Nominal, converted to binary vector
	 * Primary category : Nominal, converted to binary vector
	 * City : Nominal, converted to binary vector
	 * 
	 */
	private Instances initInstances() {
		FastVector attributes = new FastVector();
		attributes.addElement(new Attribute(PRICE));
		attributes.addElement(new Attribute(DISCOUNT));
		attributes.addElement(new Attribute(NUM_DAYS_TO_PURCHASE));
		attributes.addElement(new Attribute(NUM_DAYS_TO_REDEEM));
		attributes.addElement(new Attribute(NUM_DAYS_UNTIL_START_OF_REDEEM));
		attributes.addElement(new Attribute(CAN_BE_REDEEMED_IN_EVERY_WEEKDAY));
		attributes.addElement(new Attribute(CAN_BE_REDEEMED_IN_WEEKENDS));
		attributes.addElement(new Attribute(HAS_BLOCKER_DATES));
		attributes.addElement(new Attribute(HAS_MULTIPLE_PRICES));
		attributes.addElement(new Attribute(REQUIRES_MORE_TO_PAY));
		attributes.addElement(new Attribute(REQUIRES_PHONE_RESERVATION));
		attributes.addElement(new Attribute(IS_ONE_PERSON_COUPON));
		attributes.addElement(new Attribute(HAS_EXTRA_DISCOUNTS));
		attributes.addElement(new Attribute(HAS_MULTIPLE_LOCATIONS));
		for (String loc : this.locations) {
			attributes.addElement(new Attribute("Location" + loc));
		}
		for (String primaryCategory : this.primaryCategories) {
			attributes.addElement(new Attribute("PrimaryCategory" + primaryCategory));
		}
		
		for (String city : this.cities) {
			attributes.addElement(new Attribute("City" + city));
		}
		
		Instances dataSet = new Instances("Deals", attributes, 0);
		
		return dataSet;
	}
	
	private Instance getInstance(DealType deal) {
		int numFeatures = 14 + this.locations.length + this.primaryCategories.length + this.cities.length;
		Instance instance = new Instance(numFeatures);
		instance.setDataset(this.instances);
		
		double price = deal.getDealPrice().doubleValue();
		instance.setValueSparse(0, price);
		
		double discount = (deal.getInitialPrice().doubleValue() - price) / deal.getInitialPrice().doubleValue();
		instance.setValue(1, discount);
		
		long diff = (deal.getCouponPurchaseEndDate().toGregorianCalendar().getTimeInMillis() - 
				deal.getCouponPurchaseStartingDate().toGregorianCalendar().getTimeInMillis() );
		int numDays = (int)Math.round((double)diff / (1000.0 * 60.0 * 60.0 * 24.0));
		instance.setValue(2, numDays);
		
		diff = (deal.getCouponRedemptionEndDate().toGregorianCalendar().getTimeInMillis() - 
				deal.getCouponRedemptionStartingDate().toGregorianCalendar().getTimeInMillis() );
		numDays = (int)Math.round((double)diff / (1000.0 * 60.0 * 60.0 * 24.0));
		instance.setValue(3, numDays);
		
		diff = (deal.getCouponRedemptionStartingDate().toGregorianCalendar().getTimeInMillis() - 
				deal.getCouponPurchaseEndDate().toGregorianCalendar().getTimeInMillis());
		if (diff < 0) {
			diff = 0;
		}
		numDays = (int)Math.round((double)diff / (1000.0 * 60.0 * 60.0 * 24.0));
		instance.setValue(4, numDays);
		
		int weekdaysFound = 0;
		for (String day : deal.getValidDates()) {
			if (day.equals("Monday") || day.equals("Tuesday") || day.equals("Wednesday") || day.equals("Thursday") || day.equals("Friday")) {
				weekdaysFound ++;
			}
		}
		if (weekdaysFound == 5) {
			instance.setValue(5, 1.0);
		}
		else {
			instance.setValue(5, 0.0);
		}
		
		int weekendDays = 0;
		for (String day : deal.getValidDates()) {
			if (day.equals("Saturday") || day.equals("Sunday")) {
				weekendDays ++;
			}
		}
		if (weekendDays == 2) {
			instance.setValue(6, 1.0);
		}
		else {
			instance.setValue(6, 0.0);
		}
		
		Boolean b = deal.isHasBlockerDates();
		if (b == null) {
			instance.setValue(7, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(7, 1.0); 
			}
			else {
				instance.setValue(7, 0.0);
			}
		}
		
		b = deal.isHasMultiplePrices();
		if (b == null) {
			instance.setValue(8, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(8, 1.0); 
			}
			else {
				instance.setValue(8, 0.0);
			}
		}
		
		b = deal.isRequiresMoreToPay();
		if (b == null) {
			instance.setValue(9, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(9, 1.0); 
			}
			else {
				instance.setValue(9, 0.0);
			}
		}
		
		b = deal.isRequiresPhoneReservation();
		if (b == null) {
			instance.setValue(10, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(10, 1.0); 
			}
			else {
				instance.setValue(10, 0.0);
			}
		}
		
		b = deal.isIsOnePersonCoupon();
		if (b == null) {
			instance.setValue(11, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(11, 1.0); 
			}
			else {
				instance.setValue(11, 0.0);
			}
		}
		
		b = deal.isHasExtraDiscounts();
		if (b == null) {
			instance.setValue(12, 0.0);
		}
		else {
			if (b.booleanValue()) {
				instance.setValue(12, 1.0); 
			}
			else {
				instance.setValue(12, 0.0);
			}
		}
		
		List<AddressType> addressList = deal.getBusinessAddress().getAddress();
		if (addressList != null && addressList.size() > 1) {
			instance.setValue(13, 1.0);
		}
		else {
			instance.setValue(13, 0.0);
		}
		
		int baseIndex = 14;
		if (addressList == null) {
			String location = "Unknown";
			for (int i = 0; i < this.locations.length; i ++) {
				if (this.locations[i].equals(location)) {
					instance.setValue(baseIndex + i, 1.0);
				}
				else {
					instance.setValue(baseIndex + i, 0.0);
				}
			}
		}
		else {
			HashSet<Integer> indxSet = new HashSet<Integer>();
			for (AddressType address : deal.getBusinessAddress().getAddress()) {
				String location = address.getGeographicalArea() != null ? address.getGeographicalArea() : "Unknown";
				for (int i = 0; i < this.locations.length; i ++) {
					if (this.locations[i].equals(location)) {
						indxSet.add(i);
					}
				}
			}
			for (int i = 0; i < this.locations.length; i ++) {
				if (indxSet.contains(i)) {
					instance.setValue(baseIndex + i, 1.0);
				}
				else {
					instance.setValue(baseIndex + i, 0.0);
				}
			}
		}
		
		baseIndex = 14 + this.locations.length;
		String primaryCategory = deal.getCategory();
		for (int i = 0; i < this.primaryCategories.length; i ++) {
			if (this.primaryCategories[i].equals(primaryCategory)) {
				instance.setValue(baseIndex + i, 1.0);
			}
			else {
				instance.setValue(baseIndex + i, 0.0);
			}
		}
		
		baseIndex = 14 + this.locations.length + this.primaryCategories.length;
		String city = deal.getSiteCity();
		for (int i = 0; i < this.cities.length; i ++) {
			if (this.cities[i].equals(city)) {
				instance.setValue(baseIndex + i, 1.0);
			}
			else {
				instance.setValue(baseIndex + i, 0.0);
			}
		}
		
		return instance;
	}
	
	/*
	 * Creates a list of positive (bought) instances and negative (not bought) instances
	 */
	private List<List<Instance>> getInstancesFromProfile(Profile profile) {
		List<List<Instance>> positiveNegativeDeals = new ArrayList<List<Instance>>();
		List<Instance> dealsPositive = new ArrayList<Instance>();
		List<Instance> dealsNegative = new ArrayList<Instance>();
		positiveNegativeDeals.add(dealsPositive);
		positiveNegativeDeals.add(dealsNegative);
		for (String dealID : profile.getAttributes().keySet()) {
			Instance instance = dealInstances.get(dealID);
			if (instance == null) {
				logger.error("Could not find Instance representation for deal " + dealID);
				continue;
			}
			
			dealsPositive.add(instance);
			
			// Now find a matching negative example
			int day = dealFirstDay.get(dealID);
			List<DealType> dayDeals = this.deals.get(day);
			
			if (dayDeals != null && dayDeals.size() > 1) {
				int cnt = -1;
				while (true) {
					cnt = this.rand.nextInt(dayDeals.size());
					
					// Make sure that you have not selected the positive instance
					DealType negativeDeal = dayDeals.get(cnt);
					if (!negativeDeal.getDealId().equals(dealID)) {
						Instance negativeInstance = dealInstances.get(negativeDeal.getDealId());
						dealsNegative.add(negativeInstance);
						
						break;
					}
				}
			}
		}
		
		return positiveNegativeDeals;
	}
	
	public static boolean isValidFile(String bucketName, String path) throws AmazonClientException, AmazonServiceException, IOException {
		AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
				DealFeatureModel.class.getResourceAsStream(AWS_CREDENTIALS)));
		boolean isValidFile = true;
	    try {
	        ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, path);
	    } catch (AmazonS3Exception s3e) {
	        if (s3e.getStatusCode() == 404) {
	        // i.e. 404: NoSuchKey - The specified key does not exist
	            isValidFile = false;
	        }
	        else {
	            throw s3e;    // rethrow all S3 exceptions other than 404   
	        }
	    }

	    return isValidFile;
	}
}
