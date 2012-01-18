package com.cleargist.model;

import java.util.ArrayList;
import java.util.List;

public class DummyFilter implements Filter {
	public List<String> applyFiltering(List<String> unfilteredIds, String tenantID) {
		return unfilteredIds == null ? new ArrayList<String>() : unfilteredIds;
	}
}
