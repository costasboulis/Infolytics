package com.cleargist.catalog.deals.scrape;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekValidDatesResolver {
	private String blockerDatesPatternString1 = ".*[από]{0,3} (\\p{InGreek}+\\.?) έως [και]{0,3}\\s?(\\p{InGreek}+\\.?).*";
	private String blockerDatesPatternString2 = ".*[από]{0,3} (\\p{InGreek}+\\.?) έως (\\p{InGreek}+\\.?) .+ και (\\p{InGreek}+\\.?).*";
	private String removeString = "ισχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private Pattern blockerDatesPattern1;
	private Pattern blockerDatesPattern2;
	private HashMap<String, Integer> allowedDayNames;
	private String[] daysIndex = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	public GreekValidDatesResolver() {
		this.blockerDatesPattern1 = Pattern.compile(blockerDatesPatternString1);
		this.blockerDatesPattern2 = Pattern.compile(blockerDatesPatternString2);
		this.allowedDayNames = new HashMap<String, Integer>();
		this.allowedDayNames.put("δευτέρα", 0);
		this.allowedDayNames.put("τρίτη", 1);
		this.allowedDayNames.put("τετάρτη", 2);
		this.allowedDayNames.put("πέμπτη", 3);
		this.allowedDayNames.put("παρασκευή", 4);
		this.allowedDayNames.put("σάββατο", 5);
		this.allowedDayNames.put("κυριακή", 6);
		this.allowedDayNames.put("δευτ.", 0);
		this.allowedDayNames.put("τρ.", 1);
		this.allowedDayNames.put("τετ.", 2);
		this.allowedDayNames.put("πεμ.", 3);
		this.allowedDayNames.put("πέμ.", 3);
		this.allowedDayNames.put("παρ.", 4);
		this.allowedDayNames.put("σαβ.", 5);
		this.allowedDayNames.put("σάβ.", 5);
		this.allowedDayNames.put("κυρ.", 6);
	}
	
	public List<String> resolve(String text) {
		List<String> datesList = new LinkedList<String>();
		
		String processedText = text.replaceAll(removeString, "");
		
		Matcher blockerDatesMatcher2 = this.blockerDatesPattern2.matcher(processedText);
		if (blockerDatesMatcher2.matches()) {
			String fromDay = blockerDatesMatcher2.group(1);
			String toDay = blockerDatesMatcher2.group(3);
			
			Integer fromIndx = this.allowedDayNames.get(fromDay);
			Integer toIndx = this.allowedDayNames.get(toDay);
			if (fromIndx != null && toIndx != null) {
				for (int i = fromIndx.intValue(); i <= toIndx.intValue(); i ++) {
					datesList.add(this.daysIndex[i]);
				}
			}
		}
		else {
			Matcher blockerDatesMatcher1 = this.blockerDatesPattern1.matcher(processedText);
			if (blockerDatesMatcher1.matches()) {
				String fromDay = blockerDatesMatcher1.group(1);
				String toDay = blockerDatesMatcher1.group(2);
				
				Integer fromIndx = this.allowedDayNames.get(fromDay);
				Integer toIndx = this.allowedDayNames.get(toDay);
				if (fromIndx != null && toIndx != null) {
					for (int i = fromIndx.intValue(); i <= toIndx.intValue(); i ++) {
						datesList.add(this.daysIndex[i]);
					}
				}
			}
		}
		
		
		return datesList;
	}
}
