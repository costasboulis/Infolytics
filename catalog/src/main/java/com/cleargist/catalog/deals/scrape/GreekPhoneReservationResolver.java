package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekPhoneReservationResolver {
	private String patternString1 = ".*απαιτείται τηλεφωνική κράτηση.*";
	private String patternString2 = ".*απαιτείται τηλεφωνικό ραντεβού.*";
	private String patternString3 = ".*καλείτε για ραντεβού τουλάχιστον.*";
	private String patternString4 = ".*απαιτείται επικοινωνία.*";
	private String patternString5 = ".*απαραίτητη [η] τηλεφωνική επικοινωνία.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	private Pattern pattern4;
	private Pattern pattern5;
	
	public GreekPhoneReservationResolver(){
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
		this.pattern4 = Pattern.compile(patternString4);
		this.pattern5 = Pattern.compile(patternString5);
	}
	
	public boolean resolve(String text) {
		Matcher patternMatcher1 = this.pattern1.matcher(text);
		if (patternMatcher1.matches()) {
			return true;
		}
		
		Matcher patternMatcher2 = this.pattern2.matcher(text);
		if (patternMatcher2.matches()) {
			return true;
		}
		
		Matcher patternMatcher3 = this.pattern3.matcher(text);
		if (patternMatcher3.matches()) {
			return true;
		}
		
		Matcher patternMatcher4 = this.pattern4.matcher(text);
		if (patternMatcher4.matches()) {
			return true;
		}
		
		Matcher patternMatcher5 = this.pattern5.matcher(text);
		if (patternMatcher5.matches()) {
			return true;
		}
		
		return false;
	}
}
