package com.cleargist.catalog.deals.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekValidDatesResolver {
	private String validDatesPatternString1 = ".*[από]{0,3} (\\p{InGreek}+\\.?) έως (\\p{InGreek}+\\.?).*";
	private String validDatesPatternString2 = ".*[από]{0,3} (\\p{InGreek}+\\.?) έως (\\p{InGreek}+\\.?) .+ και (\\p{InGreek}+\\.?).*";
	private String validDatesPatternString3 = ".*καθημερινές και σαβ/κα.*";
	private String validDatesPatternString4 = ".*καθημερινές (.+) σαββατο.*";
	private String validDatesPatternString5 = ".*(\\p{InGreek}+\\.?) - (\\p{InGreek}+\\.?).*";
	private String validDatesPatternString6 = ".*καθημερινά από.*";
	private String removeString1 = "[ιi]σχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString2 = "η πρώτη (.+) πραγματοποιηθεί έως (.+)";
	private String removeString3 = "η προσφορά δεν ισχύει (.+)";
	private String removeString4 = "εκτός από (.+)";
	private String removeString5 = "\\d\\d:\\d\\d - \\d\\d:\\d\\d";
	private Pattern validDatesPattern1;
	private Pattern validDatesPattern2;
	private Pattern validDatesPattern3;
	private Pattern validDatesPattern4;
	private Pattern validDatesPattern5;
	private Pattern validDatesPattern6;
	private HashMap<String, Integer> allowedDayNames;
	private String[] daysIndex = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	public GreekValidDatesResolver() {
		this.validDatesPattern1 = Pattern.compile(validDatesPatternString1);
		this.validDatesPattern2 = Pattern.compile(validDatesPatternString2);
		this.validDatesPattern3 = Pattern.compile(validDatesPatternString3);
		this.validDatesPattern4 = Pattern.compile(validDatesPatternString4);
		this.validDatesPattern5 = Pattern.compile(validDatesPatternString5);
		this.validDatesPattern6 = Pattern.compile(validDatesPatternString6);
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
		
		String processedText = text.replaceAll(",", "")
								   .replaceAll("έως και", "έως")
								   .replaceAll(removeString1, "")
								   .replaceAll(removeString2, "")
								   .replaceAll(removeString3, "")
								   .replaceAll(removeString4, "")
								   .replaceAll(removeString5, "");
		
		
		Matcher validDatesMatcher2 = this.validDatesPattern2.matcher(processedText);
		if (validDatesMatcher2.matches()) {
			String fromDay = validDatesMatcher2.group(1);
			String toDay = validDatesMatcher2.group(3);
			
			Integer fromIndx = this.allowedDayNames.get(fromDay);
			Integer toIndx = this.allowedDayNames.get(toDay);
			if (fromIndx != null && toIndx != null) {
				for (int i = fromIndx.intValue(); i <= toIndx.intValue(); i ++) {
					datesList.add(this.daysIndex[i]);
				}
			}
			return datesList;
		}
		else {
			Matcher validDatesMatcher1 = this.validDatesPattern1.matcher(processedText);
			if (validDatesMatcher1.matches()) {
				String fromDay = validDatesMatcher1.group(1);
				String toDay = validDatesMatcher1.group(2);
				
				Integer fromIndx = this.allowedDayNames.get(fromDay);
				Integer toIndx = this.allowedDayNames.get(toDay);
				if (fromIndx != null && toIndx != null) {
					for (int i = fromIndx.intValue(); i <= toIndx.intValue(); i ++) {
						datesList.add(this.daysIndex[i]);
					}
				}
				return datesList;
			}
		}
		
		Matcher validDatesMatcher3 = this.validDatesPattern3.matcher(processedText);
		if (validDatesMatcher3.matches()) {
			for (int i = 0; i < this.daysIndex.length; i ++) {
				datesList.add(this.daysIndex[i]);
			}
			return datesList;
		}
		
		Matcher validDatesMatcher4 = this.validDatesPattern4.matcher(processedText);
		if (validDatesMatcher4.matches()) {
			for (int i = 0; i < this.daysIndex.length - 1; i ++) {
				datesList.add(this.daysIndex[i]);
			}
			return datesList;
		}
		
		Matcher validDatesMatcher5 = this.validDatesPattern5.matcher(processedText);
		if (validDatesMatcher5.matches()) {
			String fromDay = validDatesMatcher5.group(1);
			String toDay = validDatesMatcher5.group(2);
			
			Integer fromIndx = this.allowedDayNames.get(fromDay);
			Integer toIndx = this.allowedDayNames.get(toDay);
			if (fromIndx != null && toIndx != null) {
				for (int i = fromIndx.intValue(); i <= toIndx.intValue(); i ++) {
					datesList.add(this.daysIndex[i]);
				}
			}
			return datesList;
		}
		
		Matcher validDatesMatcher6 = this.validDatesPattern6.matcher(processedText);
		if (validDatesMatcher6.matches()) {
			for (int i = 0; i < this.daysIndex.length; i ++) {
				datesList.add(this.daysIndex[i]);
			}
			return datesList;
		}
		
		String[] days = processedText.split(" ");
		List<Integer> indxs = new ArrayList<Integer>();
		for (String day : days) {
			Integer indx = this.allowedDayNames.get(day);
			if (indx == null) {
				continue;
			}
			
			indxs.add(indx);
		}
		Collections.sort(indxs);
		for (Integer indx : indxs) {
			datesList.add(this.daysIndex[indx]);
		}
		
		
		if (datesList.size() == 0) {
			if (processedText.length() > 3) {
				datesList.add(processedText);
			}
			else {
				/*
				for (int i = 0; i < this.daysIndex.length; i ++) {
					datesList.add(this.daysIndex[i]);
				}
				*/
				datesList.add("Not_mentioned");
			}
		}
		
		return datesList;
	}
}
