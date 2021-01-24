package se.alanif.jregr.exec;

import java.io.IOException;

import se.alanif.jregr.io.Directory;

// We need our own ProcessBuilder becase java.lang.ProcessBuilder is final
// which means it is not possible to mock/spy or even subclass it for unit
// testing.

public class ProcessBuilder {

	public Process exec(Directory directory, Runtime runtime, String[] commandAndArguments)
	throws IOException {
		return runtime.exec(commandAndArguments, null, directory);
	}

}
