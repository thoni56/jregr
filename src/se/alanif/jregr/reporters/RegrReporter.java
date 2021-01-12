package se.alanif.jregr.reporters;

import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.exec.RegrCase.State;
import se.alanif.jregr.io.Directory;

public interface RegrReporter {

	public static RegrReporter createReporter(CommandLine commandLine, Directory directory) {
		if (commandLine.hasOption("xml"))
			try {
				return new XMLReporter(directory);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		return new ConsoleReporter();
	}

	public void start(String suite, int numberOfTests, CommandLine commandLine);
	public void starting(RegrCase theCase, long millis);
	public void fatal();
	public void virgin();
	public void pending();
	public void pass();
	public void fail();
	public void suspended();
	public void end();
	public void suspendedAndFailed();
	public void suspendedAndPassed();
	public void report(State status);

}
