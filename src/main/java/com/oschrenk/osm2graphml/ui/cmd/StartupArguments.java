package com.oschrenk.osm2graphml.ui.cmd;

import java.io.File;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "osm2graphml")
public interface StartupArguments {

	@Option
	File path();

}
