package com.oschrenk.osm2graphml.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.mycila.xmltool.XMLDoc;
import com.mycila.xmltool.XMLTag;
import com.oschrenk.util.Parser;

public class Osm2GraphMlConversion implements Parser<XMLTag> {

	private Set<String> usedKeys = new HashSet<String>();

	private final File path;
	private final OutputStream outputStream;

	public Osm2GraphMlConversion(File path) {
		this(path, System.out);
	}

	public Osm2GraphMlConversion(File path, OutputStream outputStream) {
		this.path = path;
		this.outputStream = outputStream;
	}

	public XMLTag parse() {
		final XMLInputFactory factory = XMLInputFactory.newInstance();
		final XMLStreamReader parser;

		try {
			parser = factory.createXMLStreamReader(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}

		XMLTag tag = XMLDoc.newDocument(true).addDefaultNamespace("http://graphml.graphdrawing.org/xmlns").addNamespace("xsi",
				"http://www.w3.org/2001/XMLSchema-instance").addRoot("graphml");
		tag.gotoRoot().addAttribute("xsi:schemaLocation",
				"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
		addDefaultKeys(tag);

		Stack<String> nodeIdStack = new Stack<String>();
		Map<String, String> tags = null;

		try {
			while (parser.hasNext()) {

				switch (parser.getEventType()) {

				case XMLStreamConstants.END_DOCUMENT:
					parser.close();
					return tag.gotoRoot();

				case XMLStreamConstants.START_ELEMENT:
					if (parser.getLocalName().equals("node")) {
						tags = new HashMap<String, String>();
						// add node
						tag.gotoParent().addTag("node");
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String key = parser.getAttributeLocalName(i);
							String value = parser.getAttributeValue(i);
							usedKeys.add(key);

							if (key.equals("id")) {
								tag.addAttribute("id", value);
							} else {
								addData(tag, key, value);
							}
						}
					} else if (parser.getLocalName().equals("way")) {
						tags = new HashMap<String, String>();

						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String key = parser.getAttributeLocalName(i);
							String value = parser.getAttributeValue(i);
							usedKeys.add(key);

							if (key != null && value != null) {
								tags.put(key, value);
							}
						}

						// need to collect nd, tags, built edge at end_element
					} else if (parser.getLocalName().equals("nd")) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String key = parser.getAttributeLocalName(i);
							if (key.equals("ref")) {
								nodeIdStack.add(parser.getAttributeValue(i));
							}
						}
					} else if (parser.getLocalName().equals("tag")) {
						String k = null;
						String v = null;
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String key = parser.getAttributeLocalName(i);

							if (key.equals("k")) {
								k = parser.getAttributeValue(i);
							} else if (key.equals("v")) {
								v = parser.getAttributeValue(i);
							}
						}
						if (k != null && v != null) {
							tags.put(k, v);
						}
					} else if (parser.getLocalName().equals("bounds")) {
						// ignore for now
					}
					break;

				case XMLStreamConstants.END_ELEMENT:

					if (parser.getLocalName().equals("way")) {

						String wayId = tags.get("id");
						int waySegmentIterator = 1;
						if (nodeIdStack != null) {
							String from = null;
							String to = null;
							while (nodeIdStack.size() >= 1) {
								// ternary op handles first iteration; two nodes have to be initialized
								from = (from == null) ? nodeIdStack.pop() : from;
								to = nodeIdStack.pop();
								tag.gotoParent();
								tag.addTag("edge");
								tag.addAttribute("id", wayId + "_" + waySegmentIterator++);
								tag.addAttribute("source", from);
								tag.addAttribute("target", to);
								addDataFromMap(tag, tags);

								from = to;
							}
						}
					}
					break;

				default:
					break;
				}
				parser.next();
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}

		return tag.gotoRoot();

	}

	private void addDefaultKeys(XMLTag tag) {
		//<node id="304994979" lat="51.507406" lon="-0.1083348" version="4" changeset="2114003" user="jamicu" uid="38244" visible="true" timestamp="2009-08-12T01:33:32Z"/>
		//<way id="27776903" visible="true" timestamp="2009-05-31T13:39:15Z" version="3" changeset="1368552" user="Matt" uid="70">
		//<key id="vertex_label" for="node" attr.name="Vertex Label" attr.type="string"/>
		addKeyNamedAfterId(tag, "id", For.ALL, Type.INT);
		addKeyNamedAfterId(tag, "uid", For.ALL, Type.INT);
		addKeyNamedAfterId(tag, "visible", For.ALL, Type.BOOLEAN);
		addKeyNamedAfterId(tag, "version", For.ALL, Type.INT);
		addKeyNamedAfterId(tag, "changeset", For.ALL, Type.INT);
		addKeyNamedAfterId(tag, "user", For.ALL, Type.STRING);
		addKeyNamedAfterId(tag, "timestamp", For.ALL, Type.STRING);
		addKeyNamedAfterId(tag, "lat", For.NODE, Type.DOUBLE);
		addKeyNamedAfterId(tag, "lon", For.NODE, Type.DOUBLE);

	}

	private void addKeyNamedAfterId(XMLTag tag, String id, For f, Type type) {
		addKey(tag, id, f, id, type);
	}

	private void addKey(XMLTag tag, String id, For f, String name, Type type) {
		tag.addTag("key");
		tag.addAttribute("id", id);
		tag.addAttribute("for", f.toString());
		tag.addAttribute("attr.name", name);
		tag.addAttribute("attr.type", type.toString());
		tag.gotoParent();
	}

	private void addDataFromMap(XMLTag tag, Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			addData(tag, entry.getKey(), entry.getValue());
		}
	}

	private void addData(XMLTag tag, String key, String value) {
		tag.addTag("data");
		tag.addAttribute("key", key);
		tag.addText(value);
	}

}
