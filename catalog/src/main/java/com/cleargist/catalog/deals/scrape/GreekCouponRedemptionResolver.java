package com.cleargist.catalog.deals.scrape;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the CouponRedemptionStartDate and CouponRedemptionEndDate. The input text must contain as little as possible extraneous information
 * @author kboulis
 *
 */
public class GreekCouponRedemptionResolver {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String dealValidPatternString2 = ".*ισχύει από (\\d+ \\p{InGreek}+\\s?\\d{0,4}) έως (\\d+ \\p{InGreek}+\\s?\\d{0,4}).*";
	private String dealValidPatternString1 = ".*ισχύει από (\\d{1,2}/\\d{1,2}/\\d{2,4}) έως [και]{0,3}\\s?(\\d{1,2}/\\d{1,2}/\\d{2,4}).*";
	private Pattern dealValidPattern1;
	private Pattern dealValidPattern2;
	private HashMap<String, Integer> monthsMapper;
	
	public GreekCouponRedemptionResolver() {
		this.dealValidPattern1 = Pattern.compile(dealValidPatternString1);
		this.dealValidPattern2 = Pattern.compile(dealValidPatternString2);
		
		this.monthsMapper = new HashMap<String, Integer>();
		monthsMapper.put("ιανουαρίου", 1);
		monthsMapper.put("φεβρουαρίου", 2);
		monthsMapper.put("μαρτίου", 3);
		monthsMapper.put("απριλίου", 4);
		monthsMapper.put("μαϊου", 5);
		monthsMapper.put("ιουνίου", 6);
		monthsMapper.put("ιουλίου", 7);
		monthsMapper.put("αυγούστου", 8);
		monthsMapper.put("σεπτεμβρίου", 9);
		monthsMapper.put("οκτωβρίου", 10);
		monthsMapper.put("νοεμβρίου", 11);
		monthsMapper.put("δεκεμβρίου", 12);
	}
	
	private String parseTextDate(String text, int year) {
		String[] tmp = text.split(" ");
		int day = 0;
		try {
			day = Integer.parseInt(tmp[0]);
		}
		catch (NumberFormatException ex){
			logger.error("Cannot parse number of days from string \"" + text + "\"");
			return null;
		}
		Integer month = this.monthsMapper.get(tmp[1]);
		if (month == null) {
			logger.error("Cannot parse month from string \"" + text + "\"");
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%02d", day)); sb.append("/");
		sb.append(String.format("%02d", month.intValue()));
		if (tmp.length == 3) {
			sb.append("/"); sb.append(tmp[2]);
		}
		else {
			sb.append("/"); sb.append(year);
		}
		return sb.toString();
	}
	
	public List<Date> resolve(String text, int year) {
		List<Date> dates = new LinkedList<Date>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		Matcher dealValidMatcher = this.dealValidPattern1.matcher(text);
		if (dealValidMatcher.matches()) {
			String fromDateString = dealValidMatcher.group(1).trim();
			String toDateString = dealValidMatcher.group(2).trim();
			
			try {
				String zeroPadded = zeroPadDate(fromDateString);
				dates.add(formatter.parse(zeroPadded));
			}
			catch (ParseException ex) {
				logger.error("Could not create Date from string \"" + fromDateString + "\"");
				return dates;
			}
			
			try {
				String zeroPadded = zeroPadDate(toDateString);
				dates.add(formatter.parse(zeroPadded));
			}
			catch (ParseException ex) {
				logger.error("Could not create Date from string \"" + toDateString + "\"");
				return dates;
			}
		}
		else {
			Matcher dealValidMatcher2 = this.dealValidPattern2.matcher(text);
			if (dealValidMatcher2.matches()) {
				String fromDateString = parseTextDate(dealValidMatcher2.group(1).trim(), year);
				String toDateString = parseTextDate(dealValidMatcher2.group(2).trim(), year);
				
				try {
					dates.add(formatter.parse(fromDateString));
				}
				catch (ParseException ex) {
					logger.error("Could not create Date from string \"" + fromDateString + "\"");
					return dates;
				}
				
				try {
					dates.add(formatter.parse(toDateString));
				}
				catch (ParseException ex) {
					logger.error("Could not create Date from string \"" + toDateString + "\"");
					return dates;
				}
			}
		}
		
		return dates;
	}
	
	private String zeroPadDate(String date) {
		String[] tmp = date.trim().split("/");
		int day = Integer.parseInt(tmp[0]);
		int month = Integer.parseInt(tmp[1]);
		int year = Integer.parseInt(tmp[2]);
		if (year < 50) {
			year += 2000;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%02d", day)); sb.append("/");
		sb.append(String.format("%02d", month)); sb.append("/");
		sb.append(year);
		
		return sb.toString();
	}
}
