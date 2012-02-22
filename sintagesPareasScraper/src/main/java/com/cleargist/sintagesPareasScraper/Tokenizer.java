package com.cleargist.sintagesPareasScraper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tokenizer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected static final Locale locale = new Locale("el", "GR"); 
	public static String newline = System.getProperty("line.separator");
	private HashMap<String, Integer> dictionary;
	
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+\\.\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replaceAll("\\s+", " ");
		out = out.trim();
		
		return out;
	}
	
	
	public void readRawData(File data, String path) {
		dictionary = new HashMap<String, Integer>();
		
		File outVectorData = new File(path + "data.txt");
		BufferedWriter out = null;
		try {
			FileWriter fstream = new FileWriter(outVectorData);
			out = new BufferedWriter(fstream);
		}
		catch (Exception ex) {
			logger.error("Could not write to " + outVectorData.getAbsolutePath());
			System.exit(-1);
		}
		
		File outDictionary = new File(path + "dictionary.txt");
		BufferedWriter outDict = null;
		try {
			FileWriter fstream = new FileWriter(outDictionary);
			outDict = new BufferedWriter(fstream);
		}
		catch (Exception ex) {
			logger.error("Could not write to " + outDictionary.getAbsolutePath());
			System.exit(-1);
		}
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
			String lineStr = null;
			
			while ((lineStr = br.readLine()) != null) {
				String[] fields = lineStr.split("\";\"");
				if (fields.length != 2) {
					continue;
				}
				
				String id = fields[0].replace("\"", "");
				String text = fields[1].replace("\"", "");
				
				text = removeSpecialChars(text);
				String[] words = text.split(" ");
				HashMap<Integer, Integer> wordCount = new HashMap<Integer, Integer>();
				for (String word : words) {
					if (word == null || word.isEmpty()) {
						continue;
					}
					Integer indx = dictionary.get(word);
					if (indx == null) {
						indx = dictionary.size();
						dictionary.put(word, indx);	
					}
					
					Integer cnt = wordCount.get(indx);
					if (cnt == null) {
						wordCount.put(indx, 1);
					}
					else {
						wordCount.put(indx, cnt + 1);
					}
				}
				
				// Now write the vector model
				if (wordCount.size() == 0) {
					continue;
				}
				StringBuffer sb = new StringBuffer();
				sb.append(id); sb.append(" ");
				boolean first = true;
				for (Map.Entry<Integer, Integer> me : wordCount.entrySet()) {
					if (!first) {
						sb.append(" ");
					}
					sb.append(me.getKey()); sb.append(" "); sb.append(me.getValue());
					first = false;
				}
				sb.append(newline);
				
				try {
					out.write(sb.toString());
					out.flush();
				}
				catch (Exception ex) {
					logger.error("Could not write to " + outVectorData.getAbsolutePath());
					System.exit(-1);
				}
				
			}
			br.close();
			
			// Now write the dictionary
			for (Map.Entry<String, Integer> me : dictionary.entrySet()) {
				StringBuffer sb = new StringBuffer();
				String word = me.getKey();
				if (word == null || word.isEmpty()) {
					continue;
				}
				sb.append(me.getValue()); sb.append("\t"); sb.append(word);
				sb.append(newline);
				
				try {
					outDict.write(sb.toString());
					outDict.flush();
				}
				catch (Exception ex) {
					logger.error("Could not write to " + outVectorData.getAbsolutePath());
					System.exit(-1);
				}
			}
			
			
			
		}
		catch (IOException ex) {
    		logger.error("Could not read file " + data.getAbsolutePath());
    	}
	}
	
	public static void main( String[] args ) {
		File rawRecipesText = new File("c:\\recs\\recipesTexts.txt");
		String recipesVectorPrepend = "c:\\recs\\recipesVector_";
		
		Tokenizer t = new Tokenizer();
		t.readRawData(rawRecipesText, recipesVectorPrepend);
	}
}
