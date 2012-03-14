package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekOnePersonCouponResolver {
	private String patternString1 = ".*κουπόνι είναι ατομικό.*";
	private String patternString2 = ".*κουπόνι ισχύει για 1 άτομο.*";
	private String patternString3 = ".*κουπόνι ισχύει για ένα άτομο.*";
	private String patternString4 = ".*κουπόνι ισχύει για \\d+ άτομα.*";
	private String patternString5 = ".*προσφορά ισχύει για \\d+ άτομα.*";
	private String patternString6 = ".*προσφορά ισχύει για (.+) άτομo.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	private Pattern pattern4;
	private Pattern pattern5;
	private Pattern pattern6;
	
	public GreekOnePersonCouponResolver() {
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
		this.pattern4 = Pattern.compile(patternString4);
		this.pattern5 = Pattern.compile(patternString5);
		this.pattern6 = Pattern.compile(patternString6);
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
			return false;
		}
		
		Matcher patternMatcher5 = this.pattern5.matcher(text);
		if (patternMatcher5.matches()) {
			return false;
		}
		
		Matcher patternMatcher6 = this.pattern6.matcher(text);
		if (patternMatcher6.matches()) {
			String m = patternMatcher6.group(1);
			if (m.equals("1") || m.equals("ένα")) {
				return true;
			}
		}
		
		return true;
	}
}
