package se.alanif.jregr.exec;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.CommandsDecoder;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.reporters.RegrReporter;

public class RegrRunner {

    public boolean runCases(RegrCase[] cases, RegrReporter reporter, Directory bindir, String suiteName,
                        CommandsDecoder decoder, CommandLine commandLine) throws FileNotFoundException {
        boolean success = true;
        reporter.start(suiteName, cases.length, commandLine);
        for (RegrCase theCase : cases) {
        	String csn = StandardCharsets.ISO_8859_1.toString();
            PrintWriter printWriter = null;
			try {
				printWriter = new PrintWriter(theCase.getOutputFile().getPath(), csn);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            decoder.reset();
            long start = System.currentTimeMillis();
            theCase.run(bindir, decoder, printWriter, new CaseRunner(), new ProcessBuilder());
            long end = System.currentTimeMillis();
            theCase.clean();
            if (theCase.failed()) {
                success = false;
            }

            // TODO Until we use an XML framework we can't call starting() before the test because of the timing info
            reporter.starting(theCase, end - start);
            reporter.report(theCase.status());
        }
        reporter.end();
        return success;
    }

}
