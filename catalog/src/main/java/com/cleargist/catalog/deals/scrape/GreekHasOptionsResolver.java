package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekHasOptionsResolver {
	private String patternString1 = ".*επιλογή από.*";
	private Pattern pattern1;
	
	public GreekHasOptionsResolver() {
		this.pattern1 = Pattern.compile(patternString1);
	}
	
	public boolean resolve(String text) {
		Matcher matcher1 = this.pattern1.matcher(text);
		if (matcher1.matches()) {
			return true;
		}
		
		return false;
	}
}
