package com.cleargist.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


import static org.junit.Assert.assertTrue;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Test;


import com.cleargist.data.jaxb.ActionType;
import com.cleargist.data.jaxb.Collection;
import com.cleargist.data.jaxb.Collection.DataList;
import com.cleargist.data.jaxb.DataType;
import com.cleargist.data.jaxb.MainActionType;
import com.cleargist.data.jaxb.RatingActionType;
import com.cleargist.data.jaxb.RatingType;





public class DataHandlerTest {
	public static String newline = System.getProperty("line.separator");
	
	
	
	@Test
	public void createValidDataEntries() throws Exception {
		
		Collection collection = new Collection();
		// Calendar conversions
		Date date = new Date();
		GregorianCalendar gc1 = new GregorianCalendar();
        gc1.setTime(date);
        DatatypeFactory df = DatatypeFactory.newInstance();
        collection.setCreatedAt(df.newXMLGregorianCalendar(gc1));
        collection.setDataList(new DataList());
        List<DataType> dataList = collection.getDataList().getData();
        
        DataType d1 = new DataType();
        d1.setUserId("userA");
        d1.setSession("sessionAA");
        d1.setItemId("productA");
        ActionType action1 = new ActionType();
        action1.setName(MainActionType.PURCHASE);
        d1.setEvent(action1);
        gc1.add(Calendar.MINUTE, -5);
        d1.setTimeStamp(df.newXMLGregorianCalendar(gc1));
        dataList.add(d1);
        
        DataType d2 = new DataType();
        d2.setUserId("userB");
        d2.setSession("sessionB");
        d2.setItemId("productB");
        ActionType action2 = new ActionType();
        RatingType ratingAction2 = new RatingType();
        ratingAction2.setName(RatingActionType.RATE);
        ratingAction2.setRating(1);
        action2.setRatingAction(ratingAction2);
//        action2.setName(MainActionType.ITEM_PAGE);
        d2.setEvent(action2);
        GregorianCalendar gc2 = new GregorianCalendar();
        gc2.setTime(date);
        gc2.add(Calendar.DAY_OF_MONTH, -3);
        gc2.add(Calendar.HOUR_OF_DAY, -5);
        d2.setTimeStamp(df.newXMLGregorianCalendar(gc2));
        dataList.add(d2);
        
        DataType d3 = new DataType();
        d3.setUserId("userA");
        d3.setSession("sessionAB");
        d3.setItemId("productC");
        ActionType action3 = new ActionType();
        action3.setName(MainActionType.PURCHASE);
        d3.setEvent(action3);
        GregorianCalendar gc3 = new GregorianCalendar();
        gc3.setTime(date);
        gc3.add(Calendar.MONTH, -3); 
        d3.setTimeStamp(df.newXMLGregorianCalendar(gc3));
        dataList.add(d3);
        
        DataType d4 = new DataType();
        d4.setUserId("userA");
        d4.setSession("sessionAC");
        d4.setItemId("productD");
        ActionType action4 = new ActionType();
        action4.setName(MainActionType.PURCHASE);
        d4.setEvent(action4);
        dataList.add(d4);
        
        DataType d5 = new DataType();
        d5.setSession("sessionFromUnknownUser");
        d5.setItemId("productD");
        ActionType action5 = new ActionType();
        action5.setName(MainActionType.ITEM_PAGE_VIEW);
        d5.setEvent(action5);
        GregorianCalendar gc5 = new GregorianCalendar();
        gc5.setTime(date);
        gc5.add(Calendar.MONTH, -1); 
        d5.setTimeStamp(df.newXMLGregorianCalendar(gc5));
        dataList.add(d5);
        
        DataHandler dh = new DataHandler();
        try {
        	dh.marshallData(collection, "cleargist", "data.xsd", "cleargist", "dataSample.xml");
        }
        catch (Exception ex) {
        	assertTrue(false);
        }
        
        assertTrue(true);
	}

	
}
