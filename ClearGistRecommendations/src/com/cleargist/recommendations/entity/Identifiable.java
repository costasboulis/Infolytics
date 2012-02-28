package com.cleargist.recommendations.entity;

import java.io.Serializable;

public interface Identifiable<PK extends Serializable> extends Serializable {

	public PK getId();
	
}
