package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekBlockerDatesResolver {
	private String blockerDatesPatternString1 = ".*η προσφορά δεν ισχύει (.+)";
	private String blockerDatesPatternString2 = ".*εκτός από \\p{InGreek}{0,15}\\s?(\\d{1,2}/?\\d{0,2}/?\\d{0,4}) έως (\\d{1,2}/\\d{1,2}/\\d{2,4}).*";
	private String blockerDatesPatternString3 = ".*εκτός \\p{InGreek}{0,15}\\s?(\\d{1,2}/\\d{1,2}/?\\d{0,4}) - (\\d{1,2}/\\d{1,2}/\\d{2,4}).*";
	private String blockerDatesPatternString4 = ".*εκτός από (.)+ επίσημες αργίες.*";
	private String removeString1 = "[ιi]σχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString2 = "η πρώτη (.+) πραγματοποιηθεί έως (.+)";
	private String removeString3 = "\\d\\d[:\\.]\\d\\d - \\d\\d[:\\.]\\d\\d";
	private String removeString4 = "αργότερο έως (.+)";
	private Pattern blockerDatesPattern1;
	private Pattern blockerDatesPattern2;
	private Pattern blockerDatesPattern3;
	private Pattern blockerDatesPattern4;
	
	public GreekBlockerDatesResolver() {
		this.blockerDatesPattern1 = Pattern.compile(blockerDatesPatternString1);
		this.blockerDatesPattern2 = Pattern.compile(blockerDatesPatternString2);
		this.blockerDatesPattern3 = Pattern.compile(blockerDatesPatternString3);
		this.blockerDatesPattern4 = Pattern.compile(blockerDatesPatternString4);
	}
	
	public boolean resolve(String text) {
		
		String processedText = text.replaceAll(",", "")
		   						   .replaceAll("έως και", "έως")
		   						   .replaceAll(removeString1, "")
		   						   .replaceAll(removeString2, "")
		   						   .replaceAll(removeString3, "")
		   						   .replaceAll(removeString4, "");
		   
		Matcher blockerDatesMatcher1 = this.blockerDatesPattern1.matcher(processedText);
		if (blockerDatesMatcher1.matches()) {
			return true;
		}
		
		Matcher blockerDatesMatcher2 = this.blockerDatesPattern2.matcher(processedText);
		if (blockerDatesMatcher2.matches()) {
			return true;
		}
		
		Matcher blockerDatesMatcher3 = this.blockerDatesPattern3.matcher(processedText);
		if (blockerDatesMatcher3.matches()) {
			return true;
		}
		
		Matcher blockerDatesMatcher4 = this.blockerDatesPattern4.matcher(processedText);
		if (blockerDatesMatcher4.matches()) {
			return true;
		}
		
		return false;
	}
	
}
