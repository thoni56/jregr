package se.alanif.jregr.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import se.alanif.jregr.CommandsDecoder;
import se.alanif.jregr.RegrDirectory;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

public class RegrCase {

	// Status values for cases
	public enum State {
		VIRGIN, PENDING, FAIL, FATAL, PASS, SUSPENDED, SUSPENDED_FAIL, SUSPENDED_PASS
	}

	private String caseName;
	private RegrDirectory regrDirectory;
	private boolean fatal = false;

	public RegrCase(String caseName, RegrDirectory directory) {
		this.caseName = caseName;
		this.regrDirectory = directory;
	}

	public void run(Directory binDirectory, CommandsDecoder decoder, PrintWriter outputWriter, CommandRunner commandRunner) {

		int linenumber = 1;
		outputWriter.printf("########## %s ##########%n", caseName);

		decoder.reset(caseName);
		try {
			do {
				String[] commandAndArguments = decoder.buildCommandAndArguments(binDirectory, caseName);

				String extension = decoder.getExtension();

				if (regrDirectory.exists(caseName+extension)) {

					final String stdin = decoder.getStdin();

					String output = commandRunner.runCommandForOutput(commandAndArguments, stdin, regrDirectory.toDirectory());

					final String stdout = decoder.getStdout();
					if (stdout == null)
						outputWriter.print(output);
					else if (!stdout.equals("/dev/null"))
						writeOutputToRedirection(output, stdout);
					linenumber++;
				} else {
					outputWriter.print(".jregr:"+linenumber+" "+caseName+extension+" does not exist!");
				}
			} while (decoder.advance());
		} catch (FileNotFoundException e) {
			// did not find the .input file, but that might not be a problem, could be a
			// virgin test case but it could also be a mistake in the .jregr file
			outputWriter.println("WARNING! Could not find input file for command line " + linenumber + " in .jregr file");
		} catch (IOException | InterruptedException e) {
			fatal = true;
			outputWriter.println(e.getMessage());
		} finally {
			outputWriter.close();
		}
	}

	private void writeOutputToRedirection(String output, final String stdout) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(regrDirectory.toDirectory().getPath()+File.separator+stdout));
		writer.write(output);
		writer.close();
	}

	private void removeOutputFile() {
		if (!getOutputFile().delete())
			System.out.println("Error : could not delete output file");
	}

	private boolean outputSameAsExpected() {
		File expectedFile = getExpectedFile();
		if (expectedFile.exists()) {
			File outputFile = getOutputFile();
			try (BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile));
					BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));) {
				return fileContentsAreEqual(expectedReader, outputReader);
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

	private boolean fileContentsAreEqual(BufferedReader expectedReader, BufferedReader outputReader) {
		String outputLine = "";
		String expectedLine = "";
		while (outputLine != null && expectedLine != null) {
			if (!outputLine.equals(expectedLine))
				return false;
			try {
				outputLine = outputReader.readLine();
				expectedLine = expectedReader.readLine();
			} catch (IOException e) {
				return false;
			}
		}
		return outputLine == null && expectedLine == null;
	}

	public void clean() {
		if (outputSameAsExpected())
			removeOutputFile();
	}

	public String getName() {
		return caseName;
	}

	public String toString() {
		return getName();
	}

	public State status() {
		if (fatal)
			return State.FATAL;
		boolean isSuspended = false;
		if (regrDirectory.hasSuspendedFile(caseName))
			isSuspended = true;
		if (!regrDirectory.hasExpectedFile(caseName) && !regrDirectory.hasOutputFile(caseName))
			return isSuspended ? State.SUSPENDED : State.VIRGIN;
		if (!regrDirectory.hasExpectedFile(caseName) && regrDirectory.hasOutputFile(caseName))
			return isSuspended ? State.SUSPENDED : State.PENDING;
		if (regrDirectory.hasExpectedFile(caseName) && !regrDirectory.hasOutputFile(caseName))
			return isSuspended ? State.SUSPENDED_PASS : State.PASS;
		return isSuspended ? State.SUSPENDED_FAIL : State.FAIL;
	}

	public boolean failed() {
		return fatal || status() == State.FAIL;
	}

	public boolean exists() {
		return regrDirectory.hasCaseFile(caseName);
	}

	public File getOutputFile() {
		return regrDirectory.getOutputFile(caseName);
	}

	public File getExpectedFile() {
		return regrDirectory.getExpectedFile(caseName);
	}

	public PrintWriter getPrintWriter() throws FileNotFoundException {
		return new PrintWriter(getOutputFile().getPath());
	}

}
