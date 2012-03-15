package com.cleargist.catalog.deals.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekBlockerDatesResolver {
	private String blockerDatesPatternString1 = ".*προσφορά δεν ισχύει.*";
	private String blockerDatesPatternString2 = ".*εκτός από \\p{InGreek}{0,15}\\s?(\\d{1,2}/?\\d{0,2}/?\\d{0,4}) έως (\\d{1,2}/\\d{1,2}/\\d{2,4}).*";
//	private String blockerDatesPatternString3 = ".*εκτός \\p{InGreek}{0,15}\\s?(\\d{1,2}/\\d{1,2}/?\\d{0,4}) - (\\d{1,2}/\\d{1,2}/\\d{2,4}).*";
	private String blockerDatesPatternString3 = ".*εκτός \\p{InGreek}{0,15}\\s?(\\d{1,2}/?\\d{0,2}/?\\d{0,4}).*";
	private String blockerDatesPatternString4 = ".*εκτός (.+)";
	private String blockerDatesPatternString5 = ".*εξαιρούνται οι ημερομηνίες.*";
//	private String blockerDatesPatternString6 = ".*εκτός από \\p{InGreek}{0,5}\\s?αφίξεις.*";
	private String blockerDatesPatternString7 = ".*όχι όμως από.*";
	private String blockerDatesPatternString8 = ".*εξαιρείται το (.+)διάστημα.*";
	private String blockerDatesPatternString9 = ".*θα παραμείνει κλειστό.*";
	private String removeString1 = "[ιi]σχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString2 = "η πρώτη (.+) πραγματοποιηθεί έως \\p{InGreek}{0,7}\\s?\\d{1,2}/\\d{1,2}/\\d{2,4}";
	private String removeString3 = "\\d\\d[:\\.]\\d\\d - \\d\\d[:\\.]\\d\\d";
	private String removeString4 = "αργότερο έως \\p{InGreek}{0,7}\\s?\\d{1,2}/\\d{1,2}/\\d{2,4}";
	private Pattern blockerDatesPattern1;
	private Pattern blockerDatesPattern2;
	private Pattern blockerDatesPattern3;
	private Pattern blockerDatesPattern4;
	private Pattern blockerDatesPattern5;
//	private Pattern blockerDatesPattern6;
	private Pattern blockerDatesPattern7;
	private Pattern blockerDatesPattern8;
	private Pattern blockerDatesPattern9;
	
	public GreekBlockerDatesResolver() {
		this.blockerDatesPattern1 = Pattern.compile(blockerDatesPatternString1);
		this.blockerDatesPattern2 = Pattern.compile(blockerDatesPatternString2);
		this.blockerDatesPattern3 = Pattern.compile(blockerDatesPatternString3);
		this.blockerDatesPattern4 = Pattern.compile(blockerDatesPatternString4);
		this.blockerDatesPattern5 = Pattern.compile(blockerDatesPatternString5);
//		this.blockerDatesPattern6 = Pattern.compile(blockerDatesPatternString6);
		this.blockerDatesPattern7 = Pattern.compile(blockerDatesPatternString7);
		this.blockerDatesPattern8 = Pattern.compile(blockerDatesPatternString8);
		this.blockerDatesPattern9 = Pattern.compile(blockerDatesPatternString9);
	}
	
	public boolean resolve(String text) {
		
		String processedText = text.replaceAll(",", "")
		   						   .replaceAll("έως και", "έως")
		   						   .replaceAll(removeString2, "")
		   						   .replaceAll(removeString3, "")
		   						   .replaceAll(removeString4, "");
		   
		Matcher blockerDatesMatcher1 = this.blockerDatesPattern1.matcher(processedText);
		if (blockerDatesMatcher1.matches()) {
			return true;
		}
		
		processedText = processedText.replaceAll(removeString1, "");
		
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
			String matchedText = blockerDatesMatcher4.group(1);
			if (matchedText.contains("αργίες") || matchedText.contains("αφίξεις") || matchedText.contains("χριστο") || 
				matchedText.contains("πρωτοχρονιά") || matchedText.contains("πάσχα") || matchedText.contains("παρασκευή") ||
				matchedText.contains("αύγουστο") || matchedText.contains("εορτ")) {
				return true;
			}
		}
		
		Matcher blockerDatesMatcher5 = this.blockerDatesPattern5.matcher(processedText);
		if (blockerDatesMatcher5.matches()) {
			return true;
		}
		
//		Matcher blockerDatesMatcher6 = this.blockerDatesPattern6.matcher(processedText);
//		if (blockerDatesMatcher6.matches()) {
//			return true;
//		}
		
		Matcher blockerDatesMatcher7 = this.blockerDatesPattern7.matcher(processedText);
		if (blockerDatesMatcher7.matches()) {
			return true;
		}
		
		Matcher blockerDatesMatcher8 = this.blockerDatesPattern8.matcher(processedText);
		if (blockerDatesMatcher8.matches()) {
			return true;
		}
		
		Matcher blockerDatesMatcher9 = this.blockerDatesPattern9.matcher(processedText);
		if (blockerDatesMatcher9.matches()) {
			return true;
		}
		
		return false;
	}
	
}
