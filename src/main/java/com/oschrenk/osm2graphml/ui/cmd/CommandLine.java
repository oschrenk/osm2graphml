package com.oschrenk.osm2graphml.ui.cmd;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import com.mycila.xmltool.XMLTag;
import com.oschrenk.osm2graphml.core.Osm2GraphMlConversion;
import com.oschrenk.util.Parser;

public class CommandLine {

	public static void main(String[] args) {
		Cli<StartupArguments> cli = null;
		StartupArguments parsedArguments;
		try {
			cli = CliFactory.createCli(StartupArguments.class);
			parsedArguments = cli.parseArguments(args);
		} catch (ArgumentValidationException e) {
			System.err.println(e);
			return;
		}
		Parser<XMLTag> conversion;
		try {
			conversion = new Osm2GraphMlConversion(parsedArguments.path(), System.out);
			conversion.parse();
		} catch (IllegalArgumentException e) {
			System.err.println(e);
			return;
		}

	}

}
