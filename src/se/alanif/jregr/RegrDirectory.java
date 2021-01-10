package se.alanif.jregr;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.exec.CaseRunner;
import se.alanif.jregr.exec.ProcessBuilder;
import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;
import se.alanif.jregr.reporters.RegrReporter;

public class RegrDirectory {

	public static final String COMMANDS_FILE_NAME = ".jregr";

	private static final String OUTPUT_EXTENSION = ".output";
	private static final String SUSPENDED_EXTENSION = ".suspended";
	private static final String EXPECTED_EXTENSION = ".expected";
	private String caseExtension;

	private Directory directory;

	private FilenameFilter caseNameFilter = new FilenameFilter() {
		public boolean accept(java.io.File dir, String name) {
			return isCaseName(name);
		}
	};

	private Runtime runtime;

	public RegrDirectory(Directory directory, Runtime runtime) throws IOException {
		this.directory = directory;
		this.runtime = runtime;
		File commandsFile = getCommandsFile();
		if (commandsFile != null) {
			CommandsDecoder commandsDecoder = new CommandsDecoder(directory.getBufferedReaderForFile(commandsFile));
			commandsDecoder.reset();
			caseExtension = commandsDecoder.getExtension();
		}
	}

	private String stripExtension(String fileName) {
		if (fileName.endsWith(caseExtension))
			return fileName.substring(0, fileName.length() - caseExtension.length());
		else
			return fileName;
	}

	public String getName() {
		return directory.getName();
	}

	public String getPath() {
		return directory.getAbsolutePath();
	}

	public boolean hasCases() {
		return getCases().length > 0;
	}

	public RegrCase[] getCases() {
		String[] fileNames = directory.list(caseNameFilter);
		return convertFilesToCases(fileNames);
	}

	private RegrCase[] convertFilesToCases(String[] fileNames) {
		ArrayList<RegrCase> cases = new ArrayList<RegrCase>();
		if (fileNames != null && fileNames.length > 0) {
			for (String string : fileNames) {
				cases.add(new RegrCase(runtime, stripExtension(string), this));
			}
		}
		return cases.toArray(new RegrCase[cases.size()]);
	}

	// If command line has cases as arguments we use this
	public RegrCase[] getCases(String[] files) {
		// Could be without extension if input from command line
		return convertFilesToCases(files);
	}

	public Directory toDirectory() {
		return directory;
	}

	public File getCommandsFile() {
		if (directory.hasFile(COMMANDS_FILE_NAME))
			return directory.getFile(COMMANDS_FILE_NAME);
		else
			return null;
	}

	public boolean hasOutputFile(String caseName) {
		return directory.hasFile(caseName + OUTPUT_EXTENSION);
	}

	public boolean hasExpectedFile(String caseName) {
		return directory.hasFile(caseName + EXPECTED_EXTENSION);
	}

	public boolean hasSuspendedFile(String caseName) {
		return directory.hasFile(caseName + SUSPENDED_EXTENSION);
	}

	public File getOutputFile(String caseName) {
		return directory.getFile(caseName + OUTPUT_EXTENSION);
	}

	public File getExpectedFile(String caseName) {
		return directory.getFile(caseName + EXPECTED_EXTENSION);
	}

	public boolean hasCaseFile(String caseName) {
		return directory.hasFile(caseName + caseExtension);
	}

	private boolean isCaseName(String name) {
		if (caseExtension.length() == 0)
			return false;
		else if (name.endsWith(caseExtension))
			return true;
		else
			return directory.hasFile(name + caseExtension);
	}


	public boolean runCases(RegrCase[] cases, RegrReporter reporter, Directory bindir, String suiteName,
			CommandsDecoder decoder, CommandLine commandLine) throws FileNotFoundException {
		reporter.start(suiteName, cases.length, commandLine);
		boolean success = runTheCases(cases, reporter, bindir, suiteName, decoder, commandLine);
		reporter.end();
		return success;
	}
	
	public boolean runCases(RegrReporter reporter, Directory bindir, String suiteName,
			CommandsDecoder decoder, CommandLine commandLine) throws FileNotFoundException {
		RegrCase[] cases = getCases();
		reporter.start(suiteName, cases.length, commandLine);
		boolean success = runTheCases(cases, reporter, bindir, suiteName, decoder, commandLine);
		reporter.end();
		return success;
	}

	private boolean runTheCases(RegrCase[] cases, RegrReporter reporter, Directory bindir, String suiteName,
			CommandsDecoder decoder, CommandLine commandLine) throws FileNotFoundException {
		boolean success = true;
		for (RegrCase theCase : cases) {
			PrintWriter printWriter = theCase.getPrintWriter();
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
		return success;
	}

	public boolean recurse(RegrReporter reporter, Directory bindir, String suitName, CommandsDecoder decoder, CommandLine commandLine) throws FileNotFoundException {
		Directory[] subDirectories = directory.getSubdirectories();
		if (subDirectories == null || subDirectories.length == 0) {
			return true;
		} else {
			if (subDirectories[0].hasFile(COMMANDS_FILE_NAME)) {
				RegrCase[] cases = getCases();
				return runCases(cases, reporter, bindir, suitName, decoder, commandLine);
			} else {
				return true;
			}
		}
	}

}
