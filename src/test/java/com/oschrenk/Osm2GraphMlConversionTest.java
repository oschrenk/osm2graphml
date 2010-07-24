package com.oschrenk;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import static org.junit.Assert.assertFalse;

import com.mycila.xmltool.ValidationResult;
import com.mycila.xmltool.XMLDoc;
import com.mycila.xmltool.XMLTag;
import com.oschrenk.osm2graphml.core.Osm2GraphMlConversion;

public class Osm2GraphMlConversionTest {

	private static final String OSM_EXAMPLE_1 = "/example.osm";

	@Test
	public void testBuild() throws MalformedURLException {
		URL url = this.getClass().getResource(OSM_EXAMPLE_1);
		File path = new File(url.getFile());

		Osm2GraphMlConversion c = new Osm2GraphMlConversion(path);
		XMLTag result = c.parse();
		System.out.println(result);
		ValidationResult results = XMLDoc.from(result, true).validate(new URL("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"));
		String[] m = results.getErrorMessages();
		for (int i = 0; i < m.length; i++) {
			System.err.println(m[i]);
		}
		assertFalse(results.hasError());
	}

}
