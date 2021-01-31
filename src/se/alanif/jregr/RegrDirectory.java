package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.exec.CommandRunner;
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

	private CommandsDecoder decoder;

	public RegrDirectory(Directory directory) throws IOException {
		this.directory = directory;
		File commandsFile = getCommandsFile();
		if (commandsFile != null) {
			decoder = new CommandsDecoder(directory.getBufferedReaderForFile(commandsFile));
			caseExtension = decoder.getExtension();
		}
	}

	public void setDecoder(CommandsDecoder decoder) {
		this.decoder = decoder;
		caseExtension = decoder.getExtension();
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

	// TODO Here files are probably enumerated multiple times since hasCases() does all the work too
	public RegrCase[] getCases() {
		String[] fileNames = directory.getFilenamesWithExtension(caseExtension);
		return convertFilesToCases(fileNames);
	}

	private RegrCase[] convertFilesToCases(String[] fileNames) {
		ArrayList<RegrCase> cases = new ArrayList<RegrCase>();
		if (fileNames != null && fileNames.length > 0) {
			for (String string : fileNames) {
				cases.add(new RegrCase(stripExtension(string), this));
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

	public boolean hasCaseFile(String caseName) {
		return directory.hasFile(caseName + caseExtension);
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

	public boolean runSelectedCases(RegrCase[] cases, RegrReporter reporter, Directory bindir, String suiteName,
			CommandLine commandLine) throws FileNotFoundException {
		reporter.startSuite(suiteName, cases.length);
		boolean success = runTheCases(cases, reporter, bindir, suiteName, commandLine);
		reporter.endSuite();
		return success;
	}

	public boolean runAllCases(RegrReporter reporter, Directory bindir, String suiteName,
			CommandLine commandLine) throws IOException {
		RegrCase[] cases = getCases();
		reporter.startSuite(suiteName, cases.length);
		boolean success = runTheCases(cases, reporter, bindir, suiteName, commandLine);
		reporter.endSuite();
		recurse(reporter, bindir, suiteName, decoder, commandLine);
		return success;
	}

	private boolean runTheCases(RegrCase[] cases, RegrReporter reporter, Directory bindir, String suiteName,
			CommandLine commandLine) throws FileNotFoundException {
		boolean success = true;
		for (RegrCase theCase : cases) {
			PrintWriter printWriter = theCase.getPrintWriter();
			long start = System.currentTimeMillis();
			theCase.run(bindir, decoder, printWriter, new CommandRunner());
			long end = System.currentTimeMillis();
			theCase.clean();
			if (theCase.failed()) {
				success = false;
			}

			// TODO Until we use an XML framework we can't call starting() before the test because of the timing info
			reporter.startTest(theCase, end - start);
			reporter.report(theCase.status());
		}
		return success;
	}

	public boolean recurse(RegrReporter reporter, Directory bindir, String suiteName, CommandsDecoder decoder, CommandLine commandLine) throws IOException {
		boolean success = true;
		Directory[] subDirectories = directory.getSubdirectories();
		if (subDirectories == null || subDirectories.length == 0) {
			return true;	// No subdirectories, that's ok
		} else {
			for (Directory subDirectory : subDirectories) {
				if (subDirectory.hasFile(COMMANDS_FILE_NAME)) {
					RegrDirectory regrDirectory = new RegrDirectory(subDirectory);
					if (regrDirectory.hasCommands())
						regrDirectory.setDecoder(new CommandsDecoder(readerFor(regrDirectory.getCommandsFile())));
					else
						regrDirectory.setDecoder(decoder);
					String subSuiteName = suiteName + "/" + subDirectory.getName();
					if (!regrDirectory.runAllCases(reporter, bindir, subSuiteName, commandLine))
						success = false;
				}
			}
		}
		return success;
	}

	private boolean hasCommands() {
		return getCommandsFile().length() > 0;
	}

	private BufferedReader readerFor(File commandsFile) {
		if (commandsFile.exists())
			try {
				return new BufferedReader(new FileReader(commandsFile));
			} catch (FileNotFoundException e) {
				return null;
			}
		else
			return null;
	}

	public boolean exists(String filename) {
		return directory.exists(filename);
	}
}
