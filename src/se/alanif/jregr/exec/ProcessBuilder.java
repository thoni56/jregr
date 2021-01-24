package se.alanif.jregr.exec;

import java.io.IOException;

import se.alanif.jregr.CommandsDecoder;
import se.alanif.jregr.io.Directory;

public class ProcessBuilder {

	// TODO Refactor this to not be needed, instead use javas ProcessBuilder:
	public Process exec(Directory binDirectory, CommandsDecoder decoder, Directory directory, Runtime runtime, String caseName, String[] commandAndArguments2)
	throws IOException {
		String[] commandAndArguments = decoder.buildCommandAndArguments(binDirectory, caseName);
		return runtime.exec(commandAndArguments, null, directory);
	}

}
