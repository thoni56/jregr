package se.alanif.jregr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import se.alanif.jregr.CommandDecoder.CommandSyntaxException;
import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.reporters.RegrReporter;

public class Main {
    private static final String JREGR_VERSION = "0.2.4";

    private void error(final String message) {
        System.out.println("Error: " + message);
    }

    private void wrongDirectory(Directory directory, String reason) {
        final String message = "Directory '" + directory.getName() + "' " + reason;
        error(message);
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

    private Directory findBinDirectory(CommandLine commandLine) {
        Directory binDirectory = null;
        if (commandLine.hasOption("bin")) {
            binDirectory = new Directory(commandLine.getOptionValue("bin"));
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
        Directory directory = findRegressionDirectory(commandLine);
        if (!directory.exists())
            wrongDirectory(directory, "does not exist");
        else
            try {
                final RegrDirectory regrDirectory = new RegrDirectory(directory);
                final File commandsFile = regrDirectory.getCommandsFile();
                if (commandsFile != null && commandsFile.length() > 0) {
                    final RegrCase[] cases = findSelectedCases(commandLine, regrDirectory);

                    Directory binDirectory = findBinDirectory(commandLine);
                    if (binDirectory != null)
                        binDirectory = canonise(binDirectory);

                    final String suiteName = regrDirectory.getName();
                    final RegrReporter reporter = RegrReporter.createReporter(commandLine, directory);

                    boolean result;
                    reporter.start(commandLine);
                    if (cases.length == 0)
                        result = regrDirectory.runAllCases(reporter, binDirectory, suiteName, commandLine);
                    else
                        result = regrDirectory.runSelectedCases(cases, reporter, binDirectory, suiteName, commandLine);
                    reporter.end();
                    return result;
                } else
                    wrongDirectory(regrDirectory.toDirectory(), "- top level directory must have a non-empty .jregr file");
            } catch (CommandSyntaxException e) {
                wrongDirectory(directory, "- syntax error in .jregr file");
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

    public static void main(String[] args) {
        System.exit(new Main().core(args) ? 0 : 1);
    }

}
