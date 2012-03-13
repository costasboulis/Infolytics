package com.cleargist.catalog.deals.scrape;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;


public class GoldenDealsCategoryTrainer {
	private static final Locale locale = new Locale("el", "GR"); 
	public static String newline = System.getProperty("line.separator"); 
	private Logger logger = Logger.getLogger(getClass());
	
	private HashMap<String, List<String>> getDescriptions(File annotatedFile) {
		HashMap<String, List<String>> descriptions = new HashMap<String, List<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(annotatedFile));
			String line = br.readLine();
			HashMap<String, Integer> fields = new HashMap<String, Integer>();
			String[] flds = line.split(";");
			int cnt = 0;
			for (String s : flds) {
				fields.put(s, cnt);
				
				cnt ++;
			}
			while (( line = br.readLine()) != null) {
				flds = line.split(";");
				String url = flds[fields.get("URL")];
				if (!url.startsWith("http")) {
					continue;
				}
				logger.info("Processing deal " + url);
				
				String urlJS = url.replaceAll("http://www.goldendeals.gr/deals/", "http://www.goldendeals.gr/deals/json/detailed/");
				
				String processedText = null;
				try {
					processedText = readDeal(urlJS + ".js");
				}
				catch (Exception ex) {
					logger.warn("Cannot read deal " + urlJS + ".js ... skipping");
					continue;
				}
				if (processedText == null || processedText.isEmpty()) {
					logger.warn("No text found for deal " + url);
					continue;
				}
				String category = flds[fields.get("GrouponCategory")];
				if (category == null) {
					logger.warn("No category found for deal " + url);
					continue;
				}
				List<String> categoryDescriptions = descriptions.get(category);
				if (categoryDescriptions == null) {
					categoryDescriptions = new LinkedList<String>();
					descriptions.put(category, categoryDescriptions);
				}
				
				categoryDescriptions.add(processedText);
			}
			br.close();
		}
		catch (Exception ex) {
			logger.error("Cannot load deals file " + annotatedFile.getAbsolutePath());
		}
		
		return descriptions;
	}
	
	private HashMap<String, Integer> createVocabulary(HashMap<String, List<String>> descriptions) {
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		for (List<String> l : descriptions.values()) {
			for (String desc : l) {
				String[] fields = desc.split(" ");
				for (String token : fields) {
					if (token.isEmpty()) {
						continue;
					}
					Integer indx = vocabulary.get(token);
					if (indx == null) {
						vocabulary.put(token, vocabulary.size());
					}
				}
			}
		}
		vocabulary.put("_NEW_TERM_", vocabulary.size());
		
		return vocabulary;
	}
	
	private HashMap<String, Double> createIDF(HashMap<String, List<String>> descriptions) {
		HashMap<String, Double> idf = new HashMap<String, Double>();
		double totalDocs = 0.0;
		for (List<String> l : descriptions.values()) {
			for (String desc : l) {
				String[] fields = desc.split(" ");
				HashSet<String> hs = new HashSet<String>();
				for (String token : fields) {
					hs.add(token);
				}
				
				for (String token : hs) {
					Double d = idf.get(token);
					if (d == null) {
						idf.put(token, 1.0);
					}
					else {
						idf.put(token, d + 1.0);
					}
				}
				
				totalDocs += 1.0;
			}
		}
		
		Set<String> keys = idf.keySet();
		for (String token : keys) {
			Double cnt = idf.get(token);
			
			double f = Math.log(totalDocs / cnt.doubleValue());
			
			idf.put(token, f);
		}
		
		return idf;
	}
	
	public boolean createDataSet(File annotatedFile, File idfFile, File vocabularyFile, File trainingFile) {
		HashMap<String, List<String>> descriptions = getDescriptions(annotatedFile);
		HashMap<String, Integer> vocabulary = createVocabulary(descriptions);
		HashMap<String, Double> idf = createIDF(descriptions);
		
		// Write idf file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(idfFile));
			for (Map.Entry<String, Double> me : idf.entrySet()) {
				StringBuffer sb = new StringBuffer();
	    		sb.append("\""); sb.append(me.getKey()); sb.append("\";\""); sb.append(me.getValue()); sb.append("\""); sb.append(newline);
	    		
	    		out.write(sb.toString());
	    		out.flush();
	    	}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write idf file " + idfFile.getAbsolutePath());
			return false;
		}
		
		// Write vocabulary
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(vocabularyFile));
			for (Map.Entry<String, Integer> me : vocabulary.entrySet()) {
				StringBuffer sb = new StringBuffer();
	    		sb.append("\""); sb.append(me.getKey()); sb.append("\";\""); sb.append(me.getValue()); sb.append("\""); sb.append(newline);
	    		
	    		out.write(sb.toString());
	    		out.flush();
	    	}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write vocabulary file " + vocabularyFile.getAbsolutePath());
			return false;
		}
		
		FastVector categories = new FastVector();
		categories.addElement("_DUMMY_");
		for (String category : descriptions.keySet()) {
			categories.addElement(category);
		}
		Instances instances = initInstances(vocabulary, categories);
		
		
		for (Map.Entry<String, List<String>> me: descriptions.entrySet()) {
			String category = me.getKey();
			int categoryIndex = -1;
			for (int i = 0; i < categories.size(); i ++) {
				if (category.equals(categories.elementAt(i))) {
					categoryIndex = i;
					break;
				}
			}
			if (categoryIndex == -1) {
				logger.warn("Could not find index of category " + category);
				continue;
			}
			for (String text : me.getValue()) {
				Instance instance = createTfIdf(text, categoryIndex, idf, vocabulary);
				instance.setDataset(instances);
				
				instances.add(instance);
			}
		}
		
		ArffSaver arffSaverInstance = new ArffSaver(); 
	    arffSaverInstance.setInstances(instances);
	    try {
	    	arffSaverInstance.setFile(trainingFile); 
		    arffSaverInstance.writeBatch();
	    }
	    catch (Exception ex) {
			logger.error("Could not create ARFF file");
			return false;
		}
	    
/*		
		// Create tmp text files
		boolean dirExists = new File(TMP_DESCRIPTIONS_DIRECTORY).exists();
		if (dirExists) {
			try {
				FileUtils.deleteDirectory(new File(TMP_DESCRIPTIONS_DIRECTORY));
			}
			catch (Exception ex) {
				logger.error("Could not delete directory " + TMP_DESCRIPTIONS_DIRECTORY);
				return false;
			}
		}
		boolean b = new File(TMP_DESCRIPTIONS_DIRECTORY).mkdir();
		if (!b) {
			logger.error("Could not create directory " + TMP_DESCRIPTIONS_DIRECTORY);
			return false;
		}
		for (Map.Entry<String, List<String>> me : descriptions.entrySet()) {
			String category = me.getKey();
			b = new File(TMP_DESCRIPTIONS_DIRECTORY + category).mkdir();
			int cnt = 0;
			for (String d : me.getValue()) {
				File f = new File(TMP_DESCRIPTIONS_DIRECTORY + category + "\\" + cnt);
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					out.write(d);
					out.close();
				}
				catch (Exception ex) {
					logger.error("Cannot write category descriptions file " + f.getAbsolutePath());
					return false;
				}
				cnt ++;
			}
		}
		
		// Now create ARFF file
		TextDirectoryLoader loader = new TextDirectoryLoader();
		try {
			loader.setDirectory(new File(TMP_DESCRIPTIONS_DIRECTORY));
		    Instances dataRaw = loader.getDataSet();
		    
		    StringToWordVector filter = new StringToWordVector();
		    String[] optionsSwv = weka.core.Utils.splitOptions("-I -C -N 1");
		    filter.setOptions(optionsSwv);
//		    for (String s : filter.getOptions()) {
//		    	logger.info(s);
//		    }
		    filter.setInputFormat(dataRaw);
		    Instances dataFiltered = Filter.useFilter(dataRaw, filter);
		    Reorder reorderFilter = new Reorder();
		    reorderFilter.setInputFormat(dataFiltered);
		    String[] options = weka.core.Utils.splitOptions("-R 2-last,1");
		    reorderFilter.setOptions(options);
		    Instances dataFilteredReordered = Filter.useFilter(dataFiltered, reorderFilter);
		    
		    ArffSaver arffSaverInstance = new ArffSaver(); 
		    arffSaverInstance.setInstances(dataFilteredReordered); 
		    arffSaverInstance.setFile(trainingFile); 
		    arffSaverInstance.writeBatch();
		}
		catch (Exception ex) {
			logger.error("Could not create ARFF file");
			return false;
		}
	    
		
		// Delete tmp files
	    try {
			FileUtils.deleteDirectory(new File(TMP_DESCRIPTIONS_DIRECTORY));
		}
		catch (Exception ex) {
			logger.error("Could not delete directory " + TMP_DESCRIPTIONS_DIRECTORY);
			return false;
		}
*/		
		return true;
	}
	
	private Instances initInstances(HashMap<String, Integer> vocabulary, FastVector categories) {
		FastVector attributes = new FastVector();
		List<AttributeObject> attObjs = new ArrayList<AttributeObject>();
		for (Map.Entry<String, Integer> me : vocabulary.entrySet()) {
			attObjs.add(new AttributeObject(me.getKey(), me.getValue()));
		}
		Collections.sort(attObjs);
		
		for (AttributeObject attObj : attObjs) {
			attributes.addElement(new Attribute(attObj.getName())); 
		}
		
		Attribute categoryAttribute = new Attribute("Category", categories);
		
		Instances dataSet = new Instances("CategoryText", attributes, 0);
		dataSet.insertAttributeAt(categoryAttribute, dataSet.numAttributes());
		dataSet.setClassIndex(dataSet.numAttributes() - 1);
		
		return dataSet;
	}
	
	private Instance createTfIdf(String text, int categoryIndex, HashMap<String, Double> idf, HashMap<String, Integer> vocabulary) {
		
		String[] fields = text.split(" ");
		HashMap<String, Double> hm = new HashMap<String, Double>();
		double totalTerms = 0;
		for (String tkn : fields) {
			if (tkn.isEmpty()) {
				continue;
			}
			Integer indx = vocabulary.get(tkn);
			String token = indx == null ? "_NEW_TERM" : tkn;
			
			Double cnt = hm.get(token);
			if (cnt == null) {
				hm.put(token, 1.0);
			}
			else {
				hm.put(token, cnt + 1.0);
			}
			
			totalTerms += 1.0;
		}
		
		Set<String> keys = hm.keySet();
		for (String indx : keys) {
			Double idfValue = idf.get(indx);
			
			double tf = hm.get(indx) / totalTerms;
			
			double tfidf = tf * idfValue;
			
			hm.put(indx, tfidf);
		}
		
		double[] values = new double[hm.size() + 1];
		int[] indices = new int[hm.size() + 1];
		
		List<AttributeObject> attObjs = new ArrayList<AttributeObject>();
		for (Map.Entry<String, Double> me : hm.entrySet()) {
			int indx = vocabulary.get(me.getKey());
			String val = Double.toString(me.getValue());
			attObjs.add(new AttributeObject(val, indx));
		}
		Collections.sort(attObjs);
		
		int i = 0;
		for (AttributeObject attObject : attObjs) {
			indices[i] = attObject.getIndx();
			values[i] = Double.parseDouble(attObject.getName());
			
			i ++;
		}
		indices[hm.size()] = vocabulary.size();
		values[hm.size()] = categoryIndex;
		
		Instance instance = new SparseInstance(1.0, values, indices, 1000);
		
		return instance;
	}
	
	private String readDeal(String url) throws Exception {
		JSONObject json = GoldenDealsScraper.readJsonFromUrl(url);
		
		String status = json.getString("status");
		if (!status.equals("OK")) {
			logger.warn("Could not get data from URL " + url);
			throw new Exception();
		}
		
		JSONArray array = json.getJSONArray("resultData");
		JSONObject dealJson = null;
		String dealURLId = new URL(url).getPath().replaceAll("\\.js", "").replaceAll("/deals/json/detailed/", "");
		
		int length = array.length();
		for (int i = 0; i < length; i ++) {
			String dealURL = array.getJSONObject(i).getString("url");
			
			if (dealURL.equals(dealURLId)) {
				dealJson = array.getJSONObject(i);
				
				break;
			}
			
		}
		
		if (dealJson == null) {
			logger.warn("Could not locate deal for url " + url);
			throw new Exception();
		}
		String title = dealJson.getString("title");
		String merchantName = dealJson.getString("merchantName");
		String newMerchantName = merchantName.replaceAll(" ", "_");
		
		title = title.replaceAll(merchantName, newMerchantName);
		title = removeSpecialChars(title);
		
		
		return title;
	}
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+,\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("<b>", "");
		out = out.replaceAll("</b>", "");
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>€«®-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replaceAll("\\s+", " ");
		out = out.replaceAll("^number", "");
		out = out.replaceAll("εκπτωση number", "");
		out = out.replaceAll("αξιας number", "");
		out = out.trim();
		
		return out;
	}
	
	public void evaluateModel(File trainingFile) throws Exception {
		Instances instances = null;
	     try {
	         Reader reader = new FileReader(trainingFile);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingFile.getAbsolutePath());
	         throw new Exception();
	     }
	     
	     Evaluation eval = new Evaluation(instances);
	     
	     SMO classifier = new SMO();
	     
		 eval.crossValidateModel(classifier, instances, 10, new java.util.Random());
		 double[][] d = eval.confusionMatrix();
		 for (int i = 0 ; i < d.length; i ++) {
			 StringBuffer sb = new StringBuffer();
			 for (int j = 0 ; j < d[i].length; j ++) {
				 sb.append(d[i][j]); sb.append(" ");
			 }
			 System.out.println(sb.toString());
		 }
		 System.out.println(eval.errorRate());
	}
	
	public static void main(String[] argv) {
//		String inFilename = "c:\\recs\\goldenDealsSmall.csv";
		String inFilename = "c:\\recs\\goldenDeals.csv";
		String trainingFilename = "c:\\recs\\GoldenDealsTitlesAndCategories.arff";
		String idfFilename = "c:\\recs\\GoldenDealsIdfFile.csv";
		String vocabularyFilename = "c:\\recs\\GoldenDealsVocabularyFile.csv";
		
		
		GoldenDealsCategoryTrainer gdct = new GoldenDealsCategoryTrainer();
		
//		gdct.createDataSet(new File(inFilename), new File(idfFilename), new File(vocabularyFilename), new File(trainingFilename));
		
		
		try {
			gdct.evaluateModel(new File(trainingFilename));
		}
		 catch (Exception ex) {
	         System.err.println("Cannot evaluate model");
	     }
		
		
	}
}
