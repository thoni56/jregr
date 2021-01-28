package se.alanif.jregr.exec;

import java.io.IOException;

import se.alanif.jregr.io.Directory;

public class ProcessBuilderSpy {

	private java.lang.ProcessBuilder pb;

	public ProcessBuilderSpy(String... command) {
		pb = new java.lang.ProcessBuilder(command);
	}
	
	public Process start() throws IOException {
		return pb.start();
	}

	public void directory(Directory directory) {
		pb.directory(directory);
	}

	public void command(String[] string) {
		// TODO Auto-generated method stub
		
	}
}
