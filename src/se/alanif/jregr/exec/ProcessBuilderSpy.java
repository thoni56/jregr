package se.alanif.jregr.exec;

import java.io.IOException;

import se.alanif.jregr.io.Directory;

public class ProcessBuilderSpy {

	private java.lang.ProcessBuilder pb;

	public ProcessBuilderSpy() {
		pb = new java.lang.ProcessBuilder();
	}

	public Process start() throws IOException {
		return pb.start();
	}

	public void directory(Directory directory) {
		pb.directory(directory);
	}

	public void command(String[] commandAndArguments) {
		pb.command(commandAndArguments);
	}
}
