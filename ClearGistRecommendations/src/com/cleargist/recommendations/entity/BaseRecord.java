package com.cleargist.recommendations.entity;

public class BaseRecord implements Identifiable<Long> {
	
	private static final long serialVersionUID = 1L;

	private Long id = null;

	public BaseRecord(){
		
	}
	
	protected BaseRecord(BaseRecord record) {
		this.id = record.id;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
