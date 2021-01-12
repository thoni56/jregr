package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

	private void error(boolean usegui, final String message) {
		if (usegui)
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		else
			System.out.println("Error: " + message);
	}

	private void wrongDirectory(boolean usegui, Directory directory, String reason) {
		final String message = "Directory '" + directory.getName() + "' " + reason;
		error(usegui, message);
	}

	private Directory chooseDirectory(String defaultDirectory, String title, String prompt) {
		JFileChooser chooser = new JFileChooser(defaultDirectory);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(title);
		if (chooser.showDialog(null, prompt) == JFileChooser.APPROVE_OPTION)
			return new Directory(chooser.getSelectedFile().getAbsolutePath());
		else
			return null;
	}

	private Directory selectRegrDirectory(Directory initialDirectory) throws IOException {
		Directory directory = initialDirectory;
		boolean correctSelection = false;
		while (!correctSelection) {
			correctSelection = true;
			directory = chooseDirectory(directory.getAbsolutePath(), "Select directory of test cases",
					"Run Regressions");
			if (directory != null) {
				RegrDirectory regrDirectory = new RegrDirectory(directory, Runtime.getRuntime());
				if (!regrDirectory.hasCases()) {
					wrongDirectory(true, directory, "has no cases to run");
					correctSelection = false;
				}
			} else
				directory = null;
		}
		return directory;
	}

	private Directory selectBinDirectory(Directory initialDirectory, CommandsDecoder decoder) {
		Directory directory = initialDirectory;
		boolean correctSelection = false;
		while (!correctSelection) {
			correctSelection = true;
			directory = chooseDirectory(directory.getPath(), "Select directory for executable programs", "Select");
			if (directory != null && !haveExecutables(directory, decoder)) {
				wrongDirectory(true, directory, "does not have executable programs");
				correctSelection = false;
			}
		}
		return directory;
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
			if (path.charAt(path.length() - 1) == File.separatorChar)
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
		} else if (commandLine.hasOption("gui")) {
			binDirectory = selectBinDirectory(currentDirectory(), decoder);
		}
		if (binDirectory != null && !haveExecutables(binDirectory, decoder)) {
			wrongDirectory(commandLine.hasOption("gui"), binDirectory, "does not have executable programs");
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
			wrongDirectory(commandLine.hasOption("gui"), regressionDirectory, "does not exist");
		else
			try {
				final RegrDirectory regrDirectory = new RegrDirectory(regressionDirectory, Runtime.getRuntime());
				final File commandsFile = regrDirectory.getCommandsFile();
				if (commandsFile != null && commandsFile.length() > 0) {
					final CommandsDecoder decoder = new CommandsDecoder(readerFor(commandsFile));
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
					wrongDirectory(commandLine.hasOption("gui"), regrDirectory.toDirectory(),
							"- top level directory must have a non-empty .jregr file");
			} catch (CommandSyntaxException e) {
				wrongDirectory(commandLine.hasOption("gui"), regressionDirectory,
						"- syntax error in .jregr file");
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
