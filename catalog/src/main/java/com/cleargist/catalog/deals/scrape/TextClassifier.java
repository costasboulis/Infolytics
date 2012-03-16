package com.cleargist.catalog.deals.scrape;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.cleargist.catalog.deals.entity.jaxb.Collection;
import com.cleargist.catalog.deals.entity.jaxb.DealType;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;


public abstract class TextClassifier {
	public static String newline = System.getProperty("line.separator"); 
	private Logger logger = Logger.getLogger(getClass());
	
	public abstract HashMap<String, List<String>> getData(File dealsFile, File annotatedFile);
	
	protected List<DealType>  unmarshall(File f) throws JAXBException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		
		String classPath = com.cleargist.catalog.deals.entity.jaxb.Collection.class.getCanonicalName().replaceAll("\\.Collection", "");
		JAXBContext jaxbContext = JAXBContext.newInstance(classPath);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		Collection collection = (Collection)unmarshaller.unmarshal(reader);
    	
		return collection.getDeals().getDeal();
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
	
	public boolean createDataSet(File scrapedDealFile, File annotatedDealsFile, 
								 File outIdfFile, File outVocabularyFile, File outTrainingARFFFile) {
		HashMap<String, List<String>> descriptions = getData(scrapedDealFile, annotatedDealsFile);
		HashMap<String, Integer> vocabulary = createVocabulary(descriptions);
		HashMap<String, Double> idf = createIDF(descriptions);
		
		// Write idf file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outIdfFile));
			for (Map.Entry<String, Double> me : idf.entrySet()) {
				StringBuffer sb = new StringBuffer();
	    		sb.append("\""); sb.append(me.getKey()); sb.append("\";\""); sb.append(me.getValue()); sb.append("\""); sb.append(newline);
	    		
	    		out.write(sb.toString());
	    		out.flush();
	    	}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write idf file " + outIdfFile.getAbsolutePath());
			return false;
		}
		
		// Write vocabulary
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outVocabularyFile));
			for (Map.Entry<String, Integer> me : vocabulary.entrySet()) {
				StringBuffer sb = new StringBuffer();
	    		sb.append("\""); sb.append(me.getKey()); sb.append("\";\""); sb.append(me.getValue()); sb.append("\""); sb.append(newline);
	    		
	    		out.write(sb.toString());
	    		out.flush();
	    	}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write vocabulary file " + outVocabularyFile.getAbsolutePath());
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
	    	arffSaverInstance.setFile(outTrainingARFFFile); 
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
	     classifier.buildClassifier(instances);
	     String[] classNames = classifier.classAttributeNames();
	     
		 eval.crossValidateModel(classifier, instances, 10, new java.util.Random());
		 double[][] d = eval.confusionMatrix();
		 for (int i = 1 ; i < d.length; i ++) {
			 StringBuffer sb = new StringBuffer();
			 for (int j = 1 ; j < d[i].length; j ++) {
				 sb.append(classNames[i]); sb.append("|"); sb.append(classNames[j]); sb.append(" : "); sb.append(d[i][j]); sb.append(" ");
			 }
			 System.out.println(sb.toString());
		 }
		 
		 System.out.println();System.out.println();
		 for (int i = 1; i < d.length; i ++) {
			 double precision = eval.precision(i);
			 double recall = eval.recall(i);
			 
			 System.out.println(classNames[i] + " -> P : " + precision + " R : " + recall);
		 }
		 System.out.println(eval.errorRate());
	}
	
	public static void main(String[] argv) {
//		String scrapedDealsFilename = "C:\\Users\\kboulis\\Infolytics\\catalog\\goldenDealsSmall.xml";
		String scrapedDealsFilename = "C:\\recs\\AllDealsScraped.xml";
		String annotatedDealsFilename = "C:\\recs\\AllDealsReference.xml";
		String trainingFilename = "c:\\recs\\AllDealsTitlesAndCategories.arff";
		String idfFilename = "c:\\recs\\AllDealsIdfFile.csv";
		String vocabularyFilename = "c:\\recs\\AllDealsVocabularyFile.csv";
		
		
		TextClassifier gdct = new TitleCategoryTextClassifier();
		
		gdct.createDataSet(new File(scrapedDealsFilename), new File(annotatedDealsFilename),
						   new File(idfFilename), new File(vocabularyFilename), new File(trainingFilename));
		
		
		try {
			gdct.evaluateModel(new File(trainingFilename));
		}
		 catch (Exception ex) {
	         System.err.println("Cannot evaluate model");
	     }
		
		
	}
}
