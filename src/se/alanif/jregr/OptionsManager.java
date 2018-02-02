package se.alanif.jregr;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class OptionsManager extends Options {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("static-access")
	public OptionsManager() {
		super();
		addOption("help", false, "this help");
		addOption("gui", false, "run using GUI");
		addOption("xml", false, "output XML according to ANT test format (junit et al.) instead of plain text");
		addOption("noansi", false, "don't use ANSI control on the console to minimize output");
		addOption(OptionBuilder.withLongOpt("bin")
							   .withDescription( "find binaries (alan, arun or according to the .jregr file) in directory BINDIR" )
							   .hasArg()
							   .withArgName("BINDIR")
							   .create());
		addOption(OptionBuilder.withLongOpt("dir")
				   			   .withDescription("directory of test cases to run")
				   			   .hasArg()
				   			   .withArgName("REGRDIR")
				   			   .create());
	}
	
}
