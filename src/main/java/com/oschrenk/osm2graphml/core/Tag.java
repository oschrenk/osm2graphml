package com.oschrenk.osm2graphml.core;

public class Tag {

	private String key;
	
	private String value;

	public Tag(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	
	
}
