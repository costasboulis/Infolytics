package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekPhoneReservationResolver {
	private String patternString1 = "(.*)απαιτείται τηλεφωνική.*";
	private String patternString2 = "(.*)απαιτείται τηλεφωνικό ραντεβού.*";
//	private String patternString3 = ".*καλείτε (.{0,100})\\s?τουλάχιστον\\s?\\p{InGreek}{0,3}\\s?\\d* μέρ.*";
	private String patternString3 = ".*καλείτε.*";
	private String patternString4 = "(.*)[αa]παιτείται επικοινωνία.*";
	private String patternString5 = "(.*)απαραίτητη η?\\s?τηλεφωνική επικοινωνία.*";
	private String patternString6 = ".*με τηλεφωνικό ραντεβού.*";
	private String patternString7 = ".*τηλεφωνικά.*";
	private String patternString8 = "(.*)τηλεφωνική επικοινωνία στο.*";
	private String patternString9 = "(.*)καλέστε στο.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	private Pattern pattern4;
	private Pattern pattern5;
	private Pattern pattern6;
	private Pattern pattern7;
	private Pattern pattern8;
	private Pattern pattern9;
	
	public GreekPhoneReservationResolver(){
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
		this.pattern4 = Pattern.compile(patternString4);
		this.pattern5 = Pattern.compile(patternString5);
		this.pattern6 = Pattern.compile(patternString6);
		this.pattern7 = Pattern.compile(patternString7);
		this.pattern8 = Pattern.compile(patternString8);
		this.pattern9 = Pattern.compile(patternString9);
	}
	
	public boolean resolve(String text) {
		Matcher patternMatcher1 = this.pattern1.matcher(text);
		if (patternMatcher1.matches()) {
			String matchedText = patternMatcher1.group(1);
			if (matchedText.matches(".*δεν\\s$")) {
				return false;
			}
			else {
				return true;
			}
		}
		
		Matcher patternMatcher2 = this.pattern2.matcher(text);
		if (patternMatcher2.matches()) {
			String matchedText = patternMatcher2.group(1);
			if (matchedText.matches(".*δεν\\s$")) {
				return false;
			}
			else {
				return true;
			}
		}
		
		Matcher patternMatcher3 = this.pattern3.matcher(text);
		if (patternMatcher3.matches()) {
			return true;
			/*
			String matchedText = patternMatcher3.group(1);
			if (matchedText.isEmpty() || matchedText.contains("ραντεβού") || matchedText.contains("παραγγελία") || matchedText.contains("παραλαβή") ||
				matchedText.contains("πληροφορία") || matchedText.contains("συνεννοηθείτε") || matchedText.contains("επίσκεψη")	|| 
				matchedText.contains("μαθήματα") || matchedText.contains("τμήμα") || matchedText.contains("σχολή")) {
				return true;
			}
			*/
		}
		
		Matcher patternMatcher4 = this.pattern4.matcher(text);
		if (patternMatcher4.matches()) {
			String matchedText = patternMatcher4.group(1);
			if (matchedText.matches(".*δεν\\s$")) {
				return false;
			}
			else {
				return true;
			}
		}
		
		Matcher patternMatcher5 = this.pattern5.matcher(text);
		if (patternMatcher5.matches()) {
			String matchedText = patternMatcher5.group(1);
			if (matchedText.matches(".*δεν είναι\\s$")) {
				return false;
			}
			else {
				return true;
			}
		}
		
		Matcher patternMatcher6 = this.pattern6.matcher(text);
		if (patternMatcher6.matches()) {
			return true;
		}
		
		Matcher patternMatcher7 = this.pattern7.matcher(text);
		if (patternMatcher7.matches()) {
			return true;
		}
		
		Matcher patternMatcher8 = this.pattern8.matcher(text);
		if (patternMatcher8.matches()) {
			String matchedText = patternMatcher8.group(1);
			if (matchedText.matches(".*προτείνεται\\s+$")) {
				return false;
			}
			else {
				return true;
			}
		}
		
		Matcher patternMatcher9 = this.pattern9.matcher(text);
		if (patternMatcher9.matches()) {
			return true;
		}
		
		return false;
	}
}
