package gr.infolytics.recommendations.entity;

import java.io.Serializable;

public interface Identifiable<PK extends Serializable> extends Serializable {

	public PK getId();
	
}
