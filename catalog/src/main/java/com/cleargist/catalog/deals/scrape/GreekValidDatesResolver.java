package com.cleargist.catalog.deals.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreekValidDatesResolver {
	private String validDatesPatternString1 = ".*[από]{0,3} (\\p{InGreek}+\\.?) έως (\\p{InGreek}+\\.?).*";
	private String validDatesPatternString3 = ".*καθημερινές και σαβ/κα.*";
	private String validDatesPatternString4 = ".*καθημερινές (.+) σαββατοκύριακα.*";
	private String validDatesPatternString5 = ".*(\\p{InGreek}+\\.?) - (\\p{InGreek}+\\.?).*";
	private String validDatesPatternString6 = ".*καθημερινά από.*";
	private String removeString1 = "[ιi]σχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString2 = "η πρώτη (.+) πραγματοποιηθεί έως (.+)";
	private String removeString3 = "η προσφορά δεν ισχύει (.+)";
	private String removeString4 = "εκτός από \\p{InGreek}{0,15}\\s?(\\d{1,2}/\\d{1,2}/?\\d{0,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString5 = "\\d\\d[:\\.]\\d\\d - \\d\\d[:\\.]\\d\\d";
	private String removeString6 = "αργότερο έως (.+)";
	private String removeString7 = "εκτός \\p{InGreek}{0,15}\\s?(\\d{1,2}/\\d{1,2}/?\\d{0,4}) - (\\d{1,2}/\\d{1,2}/\\d{2,4})";
	private String removeString8 = "κυριακή κλειστά";
	private String removeString9 = "δευτέρα κλειστά";
	private Pattern validDatesPattern1;
	private Pattern validDatesPattern3;
	private Pattern validDatesPattern4;
	private Pattern validDatesPattern5;
	private Pattern validDatesPattern6;
	private HashMap<String, Integer> allowedDayNames;
	private String[] daysIndex = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	private String[] greekDaysIndex = {"δευτέρα", "τρίτη", "τετάρτη", "πέμπτη", "παρασκευή", "σάββατο", "κυριακή"};
	public static String newline = System.getProperty("line.separator"); 
	
	
	public GreekValidDatesResolver() {
		this.validDatesPattern1 = Pattern.compile(validDatesPatternString1);
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
		this.allowedDayNames.put("πέμπ.", 3);
		this.allowedDayNames.put("παρ.", 4);
		this.allowedDayNames.put("σαβ.", 5);
		this.allowedDayNames.put("σάβ.", 5);
		this.allowedDayNames.put("κυρ.", 6);
	}
	
	private List<String> processExpandedList(String text) {
		List<String> datesList = new LinkedList<String>();
		
		String[] fields = text.split(" ");
		HashSet<Integer> hs = new HashSet<Integer>();
		for (String field : fields) {
			Integer indx = this.allowedDayNames.get(field);
			if (indx == null) {
				continue;
			}
			hs.add(indx);
		}
		List<Integer> indxList = new ArrayList<Integer>();
		Object[] objs = hs.toArray();
		for (int i = 0; i < hs.size(); i ++) {
			indxList.add((Integer)objs[i]);
		}
		Collections.sort(indxList);
		
		for (Integer indx : indxList) {
			datesList.add(daysIndex[indx]);
		}
		
		if (datesList.size() == 0) {
			datesList.add("Not_mentioned");
		}
		return datesList;
	}
	
	private String expandDates(String text) {
		String processedText = text;
		Matcher validDatesMatcher1 = this.validDatesPattern1.matcher(text);
		if (validDatesMatcher1.matches()) {
			String fromDay = validDatesMatcher1.group(1);
			String toDay = validDatesMatcher1.group(2);
			
			Integer fromIndx = this.allowedDayNames.get(fromDay);
			Integer toIndx = this.allowedDayNames.get(toDay);
			if (fromIndx != null && toIndx != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(" "); sb.append(this.greekDaysIndex[fromIndx.intValue()]);
				for (int i = fromIndx.intValue() + 1; i <= toIndx.intValue(); i ++) {
					sb.append(" "); sb.append(this.greekDaysIndex[i]);
				}
				
				String pattern = "[από]{0,3} (\\p{InGreek}+\\.?) έως (\\p{InGreek}+\\.?)";
				processedText = processedText.replaceAll(pattern, sb.toString());
				
				return processedText;
			}
		}
		
		Matcher validDatesMatcher5 = this.validDatesPattern5.matcher(text);
		if (validDatesMatcher5.matches()) {
			String fromDay = validDatesMatcher5.group(1);
			String toDay = validDatesMatcher5.group(2);
			
			Integer fromIndx = this.allowedDayNames.get(fromDay);
			Integer toIndx = this.allowedDayNames.get(toDay);
			if (fromIndx != null && toIndx != null) {
				StringBuffer sb = new StringBuffer();
				sb.append(this.greekDaysIndex[fromIndx.intValue()]);
				for (int i = fromIndx.intValue() + 1; i <= toIndx.intValue(); i ++) {
					sb.append(" "); sb.append(this.greekDaysIndex[i]);
				}
				
				String pattern = "(\\p{InGreek}+\\.?) - (\\p{InGreek}+\\.?)";
				processedText = processedText.replaceAll(pattern, sb.toString());
				
				return processedText;
			}
		}
		
		Matcher validDatesMatcher3 = this.validDatesPattern3.matcher(processedText);
		if (validDatesMatcher3.matches()) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.greekDaysIndex[0]);
			for (int i = 1; i < greekDaysIndex.length; i ++) {
				sb.append(" "); sb.append(this.greekDaysIndex[i]);
			}
			
			String pattern = "καθημερινές και σαβ/κα";
			processedText = processedText.replaceAll(pattern, sb.toString());
			
			return processedText;
		}
		
		Matcher validDatesMatcher4 = this.validDatesPattern4.matcher(processedText);
		if (validDatesMatcher4.matches()) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.greekDaysIndex[0]);
			for (int i = 1; i < greekDaysIndex.length; i ++) {
				sb.append(" "); sb.append(this.greekDaysIndex[i]);
			}
			
			String pattern = "καθημερινές (.+) σαββατοκύριακα";
			processedText = processedText.replaceAll(pattern, sb.toString());
			
			return processedText;
		}
		
		Matcher validDatesMatcher6 = this.validDatesPattern6.matcher(processedText);
		if (validDatesMatcher6.matches()) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.greekDaysIndex[0]);
			for (int i = 1; i < greekDaysIndex.length; i ++) {
				sb.append(" "); sb.append(this.greekDaysIndex[i]);
			}
			
			String pattern = "καθημερινά";
			processedText = processedText.replaceAll(pattern, sb.toString());
			
			return processedText;
		}
		
		return processedText;
	}
	
	public List<String> resolve(String text) {
		List<String> datesList = new LinkedList<String>();
		
		String processedText = text.replaceAll(",", "")
								   .replaceAll("έως και", "έως")
								   .replaceAll(removeString1, "")
								   .replaceAll(removeString2, "")
								   .replaceAll(removeString3, "")
								   .replaceAll(removeString4, "")
								   .replaceAll(removeString5, "")
								   .replaceAll(removeString6, "")
								   .replaceAll(removeString7, "")
								   .replaceAll(removeString8, "")
								   .replaceAll(removeString9, "");
		
		
		String expandedText = expandDates(processedText);
		datesList = processExpandedList(expandedText);
		
		return datesList;
	}
}
