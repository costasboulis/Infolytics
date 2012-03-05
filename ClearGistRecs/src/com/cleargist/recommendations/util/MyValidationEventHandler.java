package com.cleargist.recommendations.util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class MyValidationEventHandler implements ValidationEventHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
    public boolean handleEvent(ValidationEvent event) {
        logger.error("EVENT");
        logger.error("SEVERITY:  " + event.getSeverity());
        logger.error("MESSAGE:  " + event.getMessage());
        logger.error("LINKED EXCEPTION:  " + event.getLinkedException());
        logger.error("LOCATOR");
        logger.error("    LINE NUMBER:  " + event.getLocator().getLineNumber());
        logger.error("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
        logger.error("    OFFSET:  " + event.getLocator().getOffset());
        logger.error("    OBJECT:  " + event.getLocator().getObject());
        logger.error("    NODE:  " + event.getLocator().getNode());
        logger.error("    URL:  " + event.getLocator().getURL());
        return true;
    }
 
}
