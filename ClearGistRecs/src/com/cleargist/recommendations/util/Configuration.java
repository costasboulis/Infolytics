package com.cleargist.recommendations.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple class to manage loading the property file containing needed configuration data
 * from the package. Once loaded the configuration is held in memory as a singleton. The 
 * aws.properties file, is used to to store additional configuration values like s3 bundleBucket.
 */
public class Configuration {

    private static Configuration configuration=new Configuration();

    private Properties props=new Properties();

    // We leverage the properties file
   /* private static final String PROPERTY_PATH="/aws.properties";*/

    private Logger logger = Logger.getLogger(Configuration.class.getName());

    private Configuration () {
        try {
            /*props.load(this.getClass().getResourceAsStream(PROPERTY_PATH));*/
            props.load(this.getClass().getResourceAsStream("/AwsCredentials.properties"));
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Unable to load configuration: "+e.getMessage(),e);
        }
    }

    public static final Configuration getInstance () {
        return configuration;
    }

    public String getProperty (String propertyName) {
        return props.getProperty(propertyName);
    }
}
