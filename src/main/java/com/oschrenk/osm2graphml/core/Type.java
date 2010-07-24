package com.oschrenk.osm2graphml.core;

enum Type {
		BOOLEAN, INT, LONG, FLOAT, DOUBLE, STRING;

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}