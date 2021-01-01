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

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.exec.RegrRunner;
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

    private Directory selectRegrDirectory(Directory initialDirectory) {
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
            directory = new Directory(commandLine.getOptionValue("dir"));
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

    /*
     * TODO bin-directory should always be relative to the current regr directory.
     * This means that the -bin option has to be manipulated to match if there is a
     * -dir option too. Also we should ensure that we cd to the directory of the
     * tests before running them, so that the, probably relative, path to the bin
     * directory will be correct.
     */

    // Return true if success
    private boolean runCases(CommandLine commandLine) throws FileNotFoundException {
        Directory regressionDirectory = findRegressionDirectory(commandLine);
        final RegrDirectory regrDirectory = new RegrDirectory(regressionDirectory, Runtime.getRuntime());
        if (regrDirectory.getCommandsFile() != null) {
            final File commandsFile = regrDirectory.getCommandsFile();
            final CommandsDecoder decoder = new CommandsDecoder(readerFor(commandsFile));
            final Directory binDirectory = findBinDirectory(commandLine, decoder);
            if (regrDirectory.hasCases()) {
                final RegrCase[] cases = addExplicitOrImplicitCases(commandLine, regrDirectory);
                final String suiteName = createSuiteName(commandLine, regrDirectory);
                final RegrReporter reporter = RegrReporter.createReporter(commandLine, regressionDirectory);
                return RegrRunner.runCases(cases, reporter, binDirectory, suiteName, decoder, commandLine);
            } else
                wrongDirectory(commandLine.hasOption("gui"), regrDirectory.toDirectory(), "has no test cases to run");
        } else
            wrongDirectory(commandLine.hasOption("gui"), regrDirectory.toDirectory(),
                    "- top level directory must have .jregr file");
        return false;
    }

    private String createSuiteName(CommandLine commandLine, RegrDirectory regrDirectory) {
        return commandLine.hasOption("dir") ? commandLine.getOptionValue("dir") : regrDirectory.getName();
    }

    private RegrCase[] addExplicitOrImplicitCases(CommandLine commandLine, RegrDirectory regrDirectory) {
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
        System.exit(new Main().core(args) ? 0 : 1);
    }

}
