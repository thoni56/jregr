package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import se.alanif.jregr.CommandsDecoder.CommandSyntaxException;
import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.reporters.RegrReporter;

public class Main {
	private static final String JREGR_VERSION = "0.0.0";

	private void error(final String message) {
		System.out.println("Error: " + message);
	}

	private void wrongDirectory(Directory directory, String reason) {
		final String message = "Directory '" + directory.getName() + "' " + reason;
		error(message);
	}

	private boolean haveExecutables(Directory directory, CommandsDecoder decoder) {
		return directory.executablesExist(decoder);
	}

	private Directory currentDirectory() {
		return new Directory(System.getProperty("user.dir"));
	}

	private Directory findRegressionDirectory(CommandLine commandLine) {
		Directory directory;
		if (commandLine.hasOption("dir")) {
			String path = commandLine.getOptionValue("dir");
			if (path.charAt(path.length() - 1) == '\\' || path.charAt(path.length() - 1) == '/')
				path = path.substring(0, path.length()-1);
			directory = new Directory(path);
		} else {
			directory = currentDirectory();
		}
		return directory;
	}

	private Directory findBinDirectory(CommandLine commandLine, CommandsDecoder decoder) {
		Directory binDirectory = null;
		if (commandLine.hasOption("bin")) {
			binDirectory = new Directory(commandLine.getOptionValue("bin"));
		}
		if (binDirectory != null && !haveExecutables(binDirectory, decoder)) {
			wrongDirectory(binDirectory, "does not have executable programs");
			System.exit(-1);
		}
		return binDirectory;
	}

	private boolean core(String[] args) {
		OptionsManager optionManager = new OptionsManager();
		HelpFormatter helpFormatter = new HelpFormatter();
		boolean result = true;
		try {
			CommandLine commandLine = new DefaultParser().parse(optionManager, args);

			if (commandLine.hasOption("help")) {
				helpFormatter.printHelp("jregr", optionManager);
			} else if (commandLine.hasOption("version")) {
				System.out.println("Jregr version " + JREGR_VERSION);
			} else {
				result = runCases(commandLine);
			}
		} catch (ParseException e) {
			System.out.println("Argument error - " + e.getMessage());
			helpFormatter.printHelp("jregr", optionManager);
			result = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean runCases(CommandLine commandLine) throws IOException {
		Directory regressionDirectory = findRegressionDirectory(commandLine);
		if (!regressionDirectory.exists())
			wrongDirectory(regressionDirectory, "does not exist");
		else
			try {
				final RegrDirectory regrDirectory = new RegrDirectory(regressionDirectory, Runtime.getRuntime());
				final File commandsFile = regrDirectory.getCommandsFile();
				if (commandsFile != null && commandsFile.length() > 0) {
					final CommandsDecoder decoder = new CommandsDecoder(readerFor(commandsFile));
					regrDirectory.setDecoder(decoder);
					Directory binDirectory = findBinDirectory(commandLine, decoder);
					if (binDirectory != null)
						binDirectory = canonise(binDirectory);
					final String suiteName = regrDirectory.getName();
					final RegrReporter reporter = RegrReporter.createReporter(commandLine, regressionDirectory);
					final RegrCase[] cases = findSelectedCases(commandLine, regrDirectory);
					if (cases.length == 0)
						return regrDirectory.runAllCases(reporter, binDirectory, suiteName, decoder, commandLine);
					else
						return regrDirectory.runSelectedCases(cases, reporter, binDirectory, suiteName, decoder, commandLine);
				} else
					wrongDirectory(regrDirectory.toDirectory(), "- top level directory must have a non-empty .jregr file");
			} catch (CommandSyntaxException e) {
				wrongDirectory(regressionDirectory, "- syntax error in .jregr file");
			}
		return false;
	}

	private Directory canonise(Directory binDirectory) {
		Directory canonical = null;
		try {
			canonical = new Directory(binDirectory.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return canonical;
	}

	private RegrCase[] findSelectedCases(CommandLine commandLine, RegrDirectory regrDirectory) {
		final String[] arguments = commandLine.getArgs();
		RegrCase[] cases;
		if (arguments.length > 0) {
			cases = regrDirectory.getCases(arguments);
		} else {
			cases = new RegrCase[0];
		}
		return cases;
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

	public static void main(String[] args) {
		System.exit(new Main().core(args) ? 0 : 1);
	}

}
