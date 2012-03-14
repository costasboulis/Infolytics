package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekHasExtraDiscountsResolver {
	private String patternString1 = ".*επιπλέον(.+)έκπτωση \\d{1,2}%.*";
	private String patternString2 = ".*επιπλέον(.+)προσφορά \\-?\\d{1,2}%.*";
	private Pattern pattern1;
	private Pattern pattern2;
	
	public GreekHasExtraDiscountsResolver() {
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
	}
	
	public boolean resolve(String text) {
		Matcher matcher1 = this.pattern1.matcher(text);
		if (matcher1.matches()) {
			return true;
		}
		
		Matcher matcher2 = this.pattern2.matcher(text);
		if (matcher2.matches()) {
			return true;
		}
		
		return false;
	}
}
