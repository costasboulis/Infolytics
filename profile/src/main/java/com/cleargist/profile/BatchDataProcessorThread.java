package com.cleargist.profile;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

public class BatchDataProcessorThread extends Thread {
	
	private static final int MAX_RECORDS_TO_PROCESS = 25;
	private List<ReplaceableItem> items;
	private String sdbDomain;
	private AmazonSimpleDB sdb;

	public BatchDataProcessorThread(AmazonSimpleDB sdb, List<ReplaceableItem> items, String sdbDomain) {
		  this.sdb = sdb;
	      this.items = items;
	      this.sdbDomain = sdbDomain;
	    }
	
	public void run() {
		
		List<ReplaceableItem> itemsToProcess = new ArrayList<ReplaceableItem>();
		
		if (items.size() > MAX_RECORDS_TO_PROCESS) {
			for (ReplaceableItem item : items) {
				if (itemsToProcess.size() < MAX_RECORDS_TO_PROCESS) {
					itemsToProcess.add(item);
				} else {
					sdb.batchPutAttributes(new BatchPutAttributesRequest(sdbDomain, itemsToProcess));
					itemsToProcess = new ArrayList<ReplaceableItem>();
				}
			}
			//write any remaining
			if (itemsToProcess.size() > 0) {
				sdb.batchPutAttributes(new BatchPutAttributesRequest(sdbDomain, itemsToProcess));
			}
		} else {
			sdb.batchPutAttributes(new BatchPutAttributesRequest(sdbDomain, items));
		}
	}

	public List<ReplaceableItem> getItems() {
		return items;
	}

	public void setItems(List<ReplaceableItem> items) {
		this.items = items;
	}

	public String getSdbDomain() {
		return sdbDomain;
	}

	public void setSdbDomain(String sdbDomain) {
		this.sdbDomain = sdbDomain;
	}

}
