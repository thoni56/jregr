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

    // TODO Move commandLine argument to constructor
    public void start(CommandLine commandLine);
    public void startSuite(String suite, int numberOfTests);
    public void startTest(RegrCase theCase, long millis);
    public void fatal();
    public void virgin();
    public void pending();
    public void pass();
    public void fail();
    public void suspended();
    public void endSuite();
    public void suspendedAndFatal();
    public void suspendedAndFailed();
    public void suspendedAndPassed();
    public void report(State status);
    public void end();

}
