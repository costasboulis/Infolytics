package com.cleargist.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

public class MixtureOfBernoullisTest {
	private static final String AWS_CREDENTIALS = "/AwsCredentials.properties";
	public static String newline = System.getProperty("line.separator");
	private Logger logger = Logger.getLogger(getClass());
	
	private int sample(double[] pdf) {
		Random random = new Random();
		double[] cdf = new double[pdf.length];
		cdf[0] = pdf[0];
		for (int i = 1; i < pdf.length; i ++) {
			cdf[i] = cdf[i - 1] + pdf[i];
		}
		double v = random.nextDouble();
		for (int i = 0; i < cdf.length; i ++) {
			if (v <= cdf[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private void createSample() {
		int numberOfSamples = 10000;
		double[] priors = {8.0/36.0, 7.0/36.0, 6.0/36.0, 5.0/36, 4.0/36.0, 3.0/36.0, 2.0/36.0, 1.0/36.0};
		double[][] probs = { {0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2},
							 {0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2}, 
							 {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8, 0.2, 0.2, 0.2, 0.2},
							 {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.8, 0.8, 0.8, 0.8},
							 {0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2},
							 {0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2},
							 {0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2},
							 {0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8, 0.2, 0.2, 0.2, 0.8}  };
		
		
		String filename = "c:\\recs\\mixBernoullisSample.txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < numberOfSamples; i ++) {
				int m = sample(priors);
				StringBuffer sb = new StringBuffer();
				for (int att = 0; att < probs[m].length; att ++) {
					double[] p = new double[2];
					p[0] = 1.0 - probs[m][att];
					p[1] = probs[m][att];
					int indx = sample(p);
					
					if (att != 0) {
						sb.append(" ");
					}
					sb.append(att); sb.append(":"); sb.append(indx);
				}
				sb.append(newline);
				bw.write(sb.toString());
				bw.flush();
			}
			bw.flush();
			bw.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + filename);
			System.exit(-1);
		}
		
	}
	
	@Test
	public void trainInMemoryTest() {
		createSample();
		
		
	}
}
