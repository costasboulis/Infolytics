package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekOnePersonCouponResolver {
	private String patternString1 = ".*(\\p{InGreek}) είναι ατομικ.*";
	private String patternString2 = ".*(\\p{InGreek}+) ισχύει για (.+) άτομ.*";
	private String patternString3 = ".* (.+) ατόμων.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	
	public GreekOnePersonCouponResolver() {
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
	}
	
	public boolean resolve(String text) {
		
		Matcher patternMatcher1 = this.pattern1.matcher(text);
		if (patternMatcher1.matches()) {
			String matchedText = patternMatcher1.group(1);
			if (matchedText.equals("κουπόνι") || matchedText.equals("προσφορά")) {
				return true;
			}
		}
		
		Matcher patternMatcher2 = this.pattern2.matcher(text);
		if (patternMatcher2.matches()) {
			String matchedText = patternMatcher2.group(1);
			if (matchedText.equals("κουπόνι") || matchedText.equals("προσφορά")) {
				String matchedText2 = patternMatcher2.group(2);
				if (matchedText2.equals("ένα") || matchedText2.equals("1")) {
					return true;
				}
				else if (matchedText2.equals("δυο") || matchedText2.equals("δύο") || 
						 matchedText2.matches("\\d+") || matchedText2.contains("έως")) {
					return false;
				}
			}
		}
		
		Matcher patternMatcher3 = this.pattern3.matcher(text);
		if (patternMatcher3.matches()) {
			String matchedText = patternMatcher3.group(1);
			if (matchedText.equals("δυο") || matchedText.equals("δύο") || 
			    matchedText.matches("\\d+")) {
				return false;
			}
		}
		
		if (text.matches(".* \\d+ παιδιών.*") || text.matches(".* δύο παιδιών.*") || text.matches(".* \\d+ εισόδους παιδιών.*")) {
			return false;
		}
		
		
		return true;
	}
}
