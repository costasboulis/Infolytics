package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekMaxCouponsPerPersonResolver { 
	private String patternString1 = ".*αγορά[ζσ]ετε έως [και]{0,3}\\s?(\\d+) κουπόνια.*";
	private String patternString2 = ".*μπορούν να αγοραστούν πολλαπλά κουπόνια.*";
	private String patternString3 = ".*αγοραστεί μόνο ένα κουπόνι.*";
	private String patternString4 = ".*αγοραστεί μόνο 1 κουπόνι.*";
	private String patternString5 = ".*σχύει για [έως]{0,3}\\s?[και]{0,3}\\s?(.+)κουπόνι.*";
	private String patternString6 = ".*αγοραστεί [έως]{0,3}\\s?[και]\\s?(.+) κουπόνι.*"; 
	private String patternString7 = ".*αγοραστούν έως [και]{0,3}\\s?(\\d+) κουπόνια.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	private Pattern pattern4;
	private Pattern pattern5;
	private Pattern pattern6;
	private Pattern pattern7;
	
	public GreekMaxCouponsPerPersonResolver() {
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
		this.pattern4 = Pattern.compile(patternString4);
		this.pattern5 = Pattern.compile(patternString5);
		this.pattern6 = Pattern.compile(patternString6);
		this.pattern7 = Pattern.compile(patternString7);
	}
	
	public int resolve(String text) {
		
		Matcher matcher1 = this.pattern1.matcher(text);
		if (matcher1.matches()) {
			return Integer.parseInt(matcher1.group(1));
		}
		
		Matcher matcher2 = this.pattern2.matcher(text);
		if (matcher2.matches()) {
			return 100;
		}
		
		Matcher matcher3 = this.pattern3.matcher(text);
		if (matcher3.matches()) {
			return 1;
		}
		
		Matcher matcher4 = this.pattern4.matcher(text);
		if (matcher4.matches()) {
			return 1;
		}
		
		Matcher matcher5 = this.pattern5.matcher(text);
		if (matcher5.matches()) {
			String numberOne = matcher5.group(1);
			if (numberOne.equals("1") || numberOne.equals("ένα")) {
				return 1;
			}
		}
		
		Matcher matcher6 = this.pattern6.matcher(text);
		if (matcher6.matches()) {
			String numberOne = matcher6.group(1);
			if (numberOne.equals("1") || numberOne.equals("ένα")) {
				return 1;
			}
		}
		
		Matcher matcher7 = this.pattern7.matcher(text);
		if (matcher7.matches()) {
			return Integer.parseInt(matcher7.group(1));
		}
		
		return 100;
	}
}
