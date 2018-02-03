package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.exec.RegrRunner;
import se.alanif.jregr.gui.GuiReporter;
import se.alanif.jregr.gui.RegrView;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.reporters.ConsoleReporter;
import se.alanif.jregr.reporters.RegrReporter;
import se.alanif.jregr.reporters.XMLReporter;

public class Main {
	
	private void error(boolean usegui, final String message) {
		if (usegui)
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		else
			System.out.println("Error: "+message);
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

	private Directory selectRegrDirectory(Directory initialDirectory) {
		Directory directory = initialDirectory;
		boolean correctSelection = false;
		while (!correctSelection) {
			correctSelection = true;
			directory = chooseDirectory(directory.getAbsolutePath(), "Select directory of test cases", "Run Regressions");
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
			directory = chooseDirectory(directory.getPath(), "Select directory for executable programs (Alan & Arun)", "Select");
			if (directory != null) {
				if (!haveExecutables(directory, decoder)) {
					wrongDirectory(true, directory, "does not have executable Alan and Arun programs");
					correctSelection = false;
				}
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
			directory = new Directory(commandLine.getOptionValue("dir"));
		} else {
			directory = currentDirectory();
		}
		RegrDirectory regrDirectory = new RegrDirectory(directory, Runtime.getRuntime());
		if (!regrDirectory.hasCases())
			directory = selectRegrDirectory(currentDirectory());
		return directory;
	}

	private Directory findBinDirectory(CommandLine commandLine, CommandsDecoder decoder) {
		Directory binDirectory = null;
		if (commandLine.hasOption("bin")) {
			binDirectory = new Directory(commandLine.getOptionValue("bin"));
		} else if (commandLine.hasOption("gui")){
			binDirectory = selectBinDirectory(currentDirectory(), decoder);
		}
		if (binDirectory != null && !haveExecutables(binDirectory, decoder)) {
			wrongDirectory(commandLine.hasOption("gui"), binDirectory, "does not have executable programs");
			System.exit(-1);
		}
		return binDirectory;
	}

	private void core(String[] args) {
		OptionsManager optionManager = new OptionsManager();
		HelpFormatter helpFormatter = new HelpFormatter();
		try {
			CommandLine commandLine = new BasicParser().parse(optionManager, args);
	
			if (commandLine.hasOption("help")) {
				helpFormatter.printHelp("jregr", optionManager);
			} else
				handleArgs(commandLine);			
	
		} catch (ParseException e) {
			System.out.println("Argument error - " + e.getMessage());
			helpFormatter.printHelp("jregr", optionManager);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleArgs(CommandLine commandLine) throws FileNotFoundException, IOException {
		// Refactor: should not run the cases, but return a runner or null
		Directory regressionDirectory = findRegressionDirectory(commandLine);
		if (regressionDirectory != null) {
			RegrDirectory regrDirectory = new RegrDirectory(
					regressionDirectory, Runtime.getRuntime());
			final File commandsFile = regrDirectory.getCommandsFile();
			CommandsDecoder decoder = new CommandsDecoder(
					readerFor(commandsFile));
			Directory binDirectory = findBinDirectory(commandLine, decoder);
			if (regrDirectory.hasCases()) {
				RegrRunner runner = new RegrRunner();
				RegrReporter reporter;
				if (commandLine.hasOption("xml"))
					reporter = new XMLReporter(regrDirectory.toDirectory());
				else if (commandLine.hasOption("gui")) {
					reporter = new GuiReporter();
					SwingUtilities.invokeLater(new RegrView(
							(JComponent) reporter));
				} else
					reporter = new ConsoleReporter();
				RegrCase[] cases = addExplicitOrImplicitCases(commandLine,
						regrDirectory);
				final String suiteName = commandLine.hasOption("dir") ? commandLine
						.getOptionValue("dir") : regrDirectory.getName();
				runner.runCases(cases, reporter, binDirectory, suiteName,
						decoder, commandLine);
			} else {
				wrongDirectory(commandLine.hasOption("gui"),
						regrDirectory.toDirectory(), "has no Alan files to run");
				System.exit(-1);
			}
		}
	}

	private RegrCase[] addExplicitOrImplicitCases(CommandLine commandLine,
			RegrDirectory regrDirectory) {
		final String[] arguments = commandLine.getArgs();
		RegrCase[] cases;
		if (arguments.length > 0) {
			cases = regrDirectory.getCases(arguments);
		} else {
			cases = regrDirectory.getCases();
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
		new Main().core(args);
	}

}
