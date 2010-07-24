package com.oschrenk.osm2graphml.core;

enum For {
		GRAPH, NODE, EDGE, ALL;

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}