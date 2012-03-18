package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekHasExtraDiscountsResolver {
	private String patternString1 = ".*έκπτωση \\d{1,2}%.*";
	private String patternString2 = ".*προσφορά \\-?\\d{1,2}%.*";
	private String patternString3 = ".*\\d{1,2}% έκπτωση.*";
	private String patternString4 = ".*ισχύει έκπτωση.*";
	private String patternString5 = ".*επιπλέον .+ προσφέρεται .+ \\d{2,3}.*";
	private Pattern pattern1;
	private Pattern pattern2;
	private Pattern pattern3;
	private Pattern pattern4;
	private Pattern pattern5;
	
	public GreekHasExtraDiscountsResolver() {
		this.pattern1 = Pattern.compile(patternString1);
		this.pattern2 = Pattern.compile(patternString2);
		this.pattern3 = Pattern.compile(patternString3);
		this.pattern4 = Pattern.compile(patternString4);
		this.pattern5 = Pattern.compile(patternString5);
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
		
		Matcher matcher3 = this.pattern3.matcher(text);
		if (matcher3.matches()) {
			return true;
		}
		
		Matcher matcher4 = this.pattern4.matcher(text);
		if (matcher4.matches()) {
			return true;
		}
		
		Matcher matcher5 = this.pattern5.matcher(text);
		if (matcher5.matches()) {
			return true;
		}
		
		return false;
	}
}
