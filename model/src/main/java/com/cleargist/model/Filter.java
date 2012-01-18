package com.cleargist.model;

import java.util.List;

public interface Filter {
	public List<String> applyFiltering(List<String> unfilteredIds, String tenantID);
}
