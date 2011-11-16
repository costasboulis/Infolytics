package gr.infolytics.trainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CorrelationsCreator {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private HashMap<String, HashSet<Integer>> data;
	private HashMap<Integer, HashMap<Integer, Float>> SS1;
	private HashMap<Integer, Float> SS0;
	private float numberOfProfiles;
	private static final float COOCCURRENCE_THRESHOLD = 2.0f;
	private static final int TOP_CORRELATIONS = 10;
	private static final double CORRELATION_THRESHOLD = 0.05;
	
	private Connection getConnection() {
		try {
			Class.forName("org.postgresql.Driver");
 
		} 
		catch (ClassNotFoundException e) {
			logger.error("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			return null;
		}
 
		logger.info("PostgreSQL JDBC Driver Registered!");
 
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://er.three99.com:5432/EventRegistration", "postgres",
					"infolytics_database");
		} 
		catch (SQLException e) {
			logger.error("Connection Failed!");
			return null;
		}
 
		if (connection != null) {
			logger.info("You made it, take control of your database now!");
		}
		else {
			logger.info("Failed to make connection!");
		}
		
		return connection;
	}
	
	public void getData() {
		Connection connection = getConnection();
		
		if (connection == null) {
			logger.error("Cannot load data");
		}
		
		Statement st = null;
		try {
			st = connection.createStatement();
		}
		catch (SQLException e) {
			logger.error("Cannot prepare statement");
		}
		
		String sql = "SELECT user_id, item_id, action, session, created_at, updated_at FROM events";
		ResultSet rs = null;
		try {
			rs = st.executeQuery(sql);
		}
		catch (SQLException e) {
			logger.error("Cannot fetch results");
		}
		
		logger.info("Results fetched...start processing");
		data = new HashMap<String, HashSet<Integer>>();
		
		try {
			int i = 0;
			while (rs.next()) {
				String userId = rs.getString(1);
			    int itemId;
			    try {
			    	itemId = Integer.parseInt(rs.getString(2));
			    }
			    catch (NumberFormatException ex) {
			    	logger.error("Cannot parse integer itemId from " + rs.getString(2));
			    	continue;
			    }
			    String action = rs.getString(3);
			    String sessionId = rs.getString(4);
			    Timestamp createdAt = rs.getTimestamp(5);
			    Timestamp updatedAt = rs.getTimestamp(6);
			    
			    if (userId == null && sessionId == null) {
			    	continue;
			    }
			    
			    String key = userId == null || userId.isEmpty() || userId.equals("0") ? sessionId : userId;
			    
			    HashSet<Integer> items = data.get(key);
			    if (items == null) {
			    	items = new HashSet<Integer>();
			    	data.put(key, items);
			    }
			    items.add(itemId);
			    
			    
				i++;
			}
			logger.info("Read " + i + " lines");
			rs.close();
		} 
		catch (SQLException e) {
			logger.error("Cannot iterate through results");
		}
		
		logger.info("There are " + data.size() + " unique profiles");
	}
	
	public void collectSufficientStatistics(File file) {
		SS1 = new HashMap<Integer, HashMap<Integer, Float>>();
		SS0 = new HashMap<Integer, Float>();
		numberOfProfiles = 0.0f;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			// Something is funky with the first line
			String lineStr = br.readLine();
			while ((lineStr = br.readLine()) != null) {
				String[] fields = lineStr.split(";");
				numberOfProfiles += 1.0f;
				
				if (numberOfProfiles % 10000 == 0) {
					logger.info("Processed " + Math.round(numberOfProfiles) + " profiles");
				}
				for (int i = 1; i < fields.length; i ++) {
					int itemA = Integer.parseInt(fields[i]);
					
					// SS0 stats
					Float cnt = SS0.get(itemA);
					if (cnt == null) {
						SS0.put(itemA, 1.0f);
					}
					else {
						SS0.put(itemA, cnt.floatValue() + 1.0f);
					}
					
					//SS1 stats
					HashMap<Integer, Float> hm = SS1.get(itemA);
					if (hm == null) {
						hm = new HashMap<Integer, Float>();
						SS1.put(itemA, hm);
					}
					for (int j = i + 1; j < fields.length; j ++) {
						
						int itemB = Integer.parseInt(fields[j]);
						
						Float count = hm.get(itemB);
						if (count == null) {
							hm.put(itemB, 1.0f);
						}
						else {
							hm.put(itemB, count.floatValue() + 1.0f);
						}
					}
				}
			}
			br.close();
		}
		catch (IOException ex) {
			logger.error("Cannot read from file " + file.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public void estimateCorrelations(File file) {
		logger.info("Begin estimating correlations");
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Map.Entry<Integer, Float> es : SS0.entrySet()) {
				List<AttributeObject> itemsList = new LinkedList<AttributeObject>(); 
				Integer itemId = es.getKey();
				float sourceOccurrences = es.getValue();
				double tmpSourceNorm = Math.sqrt(sourceOccurrences - ((sourceOccurrences * sourceOccurrences) / numberOfProfiles));
				double sourceNorm = tmpSourceNorm == 0.0 ? 1.0e+05 : tmpSourceNorm;
				
				// Search every line for itemId
				for (Map.Entry<Integer, HashMap<Integer, Float>> es1 : SS1.entrySet()) {
					Float cc = es1.getValue().get(itemId);
					if (cc == null || cc < COOCCURRENCE_THRESHOLD) {
						continue;
					}
					Integer targetItemId = es1.getKey();
					
					Float targetOccurrences = SS0.get(targetItemId);
					if (targetOccurrences == null) {
						continue;
					}
					double tmpTargetNorm = Math.sqrt(targetOccurrences - ((targetOccurrences * targetOccurrences) / numberOfProfiles));
					double targetNorm = tmpTargetNorm == 0.0 ? 1.0e+05 : tmpTargetNorm;
					
					double nominator = (double)cc - (((double)sourceOccurrences*(double)targetOccurrences)/(double)numberOfProfiles);
					double score = nominator / (sourceNorm*targetNorm);
					
					if (score < CORRELATION_THRESHOLD) {
						continue;
					}
					itemsList.add(new AttributeObject(Integer.toString(targetItemId), score));
				}
				
				// Search the itemId line
				HashMap<Integer, Float> hm = SS1.get(itemId);
				if (hm != null) {
					for (Map.Entry<Integer, Float> es2 : hm.entrySet()) {
						float cc = es2.getValue();
						if (cc < COOCCURRENCE_THRESHOLD) {
							continue;
						}
						Integer targetItemId = es2.getKey();
						
						Float targetOccurrences = SS0.get(targetItemId);
						if (targetOccurrences == null) {
							continue;
						}
						double tmpTargetNorm = Math.sqrt(targetOccurrences - ((targetOccurrences * targetOccurrences) / numberOfProfiles));
						double targetNorm = tmpTargetNorm == 0.0 ? 1.0e+05 : tmpTargetNorm;
						
						double nominator = (double)cc - (((double)sourceOccurrences*(double)targetOccurrences)/(double)numberOfProfiles);
						double score = nominator / (sourceNorm*targetNorm);
						
						if (score < CORRELATION_THRESHOLD) {
							continue;
						}
						itemsList.add(new AttributeObject(Integer.toString(targetItemId), score));
					}
				}
				
				// Now write the correlations
				if (itemsList.size() == 0) {
					continue;
				}
				Collections.sort(itemsList);
				
				StringBuffer sb = new StringBuffer();
				sb.append(itemId);
				List<AttributeObject> l = itemsList.size() < TOP_CORRELATIONS ? itemsList : itemsList.subList(0, TOP_CORRELATIONS);
				for (AttributeObject att : l) {
					sb.append(";"); sb.append(att.getId()); sb.append(";"); sb.append((float)Math.round(att.getScore() * 1000)/10.0f);
				}
				sb.append(newline);
				out.write(sb.toString());
			}
			out.close();
			logger.info("Finished estimating correlations");
		}
		catch (IOException ex) {
			logger.error("Cannot read from file " + file.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public void writeProfiles(File file) {
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Map.Entry<String, HashSet<Integer>> es : data.entrySet()) {
				StringBuffer sb = new StringBuffer();
				sb.append(es.getKey());
				List<Integer> l = new ArrayList<Integer>(es.getValue());
				Collections.sort(l);
				for (Integer item : l) {
					sb.append(";"); sb.append(item);
				}
				sb.append(newline);
				
				out.write(sb.toString());
			}
			out.close();
		}
		catch (Exception e){
			logger.error("Error: " + e.getMessage());
			System.exit(-1);
		}
		
	}
	
    public static void main( String[] args ) {
    	String profilesFilename = "c:\\recs\\sintagesPareasProfiles.csv";
    	String correlationsFilename = "c:\\recs\\sintagesPareasCorrelations_top10.csv";
    	CorrelationsCreator corr = new CorrelationsCreator();
  
    	
  //  	corr.getData();
  //  	corr.writeProfiles(new File(profilesFilename));
    	
    	
    	corr.collectSufficientStatistics(new File(profilesFilename));
    	corr.estimateCorrelations(new File(correlationsFilename));
		
    }
}
