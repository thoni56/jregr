package se.alanif.jregr.exec;

import java.io.IOException;

import se.alanif.jregr.io.Directory;

public class ProcessBuilder {

	// TODO Refactor this to not be needed, instead use javas ProcessBuilder:
	public Process exec(Directory directory, Runtime runtime, String[] commandAndArguments)
	throws IOException {
		return runtime.exec(commandAndArguments, null, directory);
	}

}
