package se.alanif.jregr.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import se.alanif.jregr.CommandDecoder;
import se.alanif.jregr.CommandDecoder.CommandSyntaxException;
import se.alanif.jregr.RegrDirectory;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

public class RegrCase {

    // Status values for cases
    public enum State {
        VIRGIN, PENDING, FAIL, FATAL, PASS, SUSPENDED, SUSPENDED_FATAL, SUSPENDED_FAIL, SUSPENDED_PASS
    }

    private String caseName;
    private RegrDirectory regrDirectory;
    private boolean fatal = false;

    public RegrCase(String caseName, RegrDirectory directory) {
        this.caseName = caseName;
        this.regrDirectory = directory;
    }

    public void run(Directory binDirectory, CommandDecoder decoder, PrintWriter outputWriter, CommandRunner commandRunner) {

        int linenumber = 1;
        outputWriter.printf("########## %s ##########\n", caseName);

        decoder.reset(caseName);
        try {
            do {
                String[] commandAndArguments = decoder.buildCommandAndArguments(binDirectory, caseName);

                String extension = decoder.getExtension();

                if (regrDirectory.exists(caseName+extension)) {

                    if (commandAndArguments != null) {
                        final String stdin = decoder.getStdin();

                        String output = commandRunner.runCommandForOutput(commandAndArguments, stdin, regrDirectory.toDirectory());

                        final String stdout = decoder.getStdout();
                        if (!decoder.isOptional())
                            if (stdout == null)
                                outputWriter.print(output);
                            else if (!stdout.equals("/dev/null"))
                                writeOutputToRedirection(output, stdout);

                        // A crashed SUT makes the whole case fatal, and the
                        // remaining commands in the .jregr file pointless -
                        // they would only run against its debris
                        final int exitValue = commandRunner.getExitValue();
                        if (wasTerminatedAbnormally(exitValue)) {
                            fatal = true;
                            outputWriter.println(".jregr:" + linenumber + ": '" + commandAndArguments[0] + "' "
                                    + describeTermination(exitValue));
                            break;
                        }
                    }

                } else if (!decoder.isOptional()) {
                    outputWriter.print(".jregr:"+linenumber+" "+caseName+extension+" does not exist!\n");
                }

                linenumber++;
            } while (decoder.advance());
        } catch (FileNotFoundException e) {
            // did not find the .input file, but that might not be a problem, could be a
            // virgin test case but it could also be a mistake in the .jregr file
            outputWriter.println("WARNING! Could not find input file for command line " + linenumber + " in .jregr file");
        } catch (CommandSyntaxException e) {
            outputWriter.println(".jregr:"+linenumber+": "+e.getMessage());
        } catch (IOException | InterruptedException e) {
            fatal = true;
            outputWriter.println(e.getMessage());
        } finally {
            outputWriter.close();
        }
    }

    // Only a death by signal is a crash. Alan's suites are full of cases
    // that legitimately exit non-zero, so an ordinary exit status, however
    // unhappy, must not turn them fatal.
    private static boolean wasTerminatedAbnormally(int exitValue) {
        // Unix reports a signal death as 128+signal, Windows reports an
        // unhandled exception as a status code that is negative as a Java int
        return exitValue > 128 || exitValue < 0;
    }

    private static String describeTermination(int exitValue) {
        if (exitValue < 0)
            return String.format("terminated by exception 0x%08X", exitValue);
        final int signal = exitValue - 128;
        final String name = signalName(signal);
        return "terminated by signal " + signal + (name == null ? "" : " (" + name + ")");
    }

    private static String signalName(int signal) {
        switch (signal) {
        case 1: return "SIGHUP";
        case 2: return "SIGINT";
        case 3: return "SIGQUIT";
        case 4: return "SIGILL";
        case 5: return "SIGTRAP";
        case 6: return "SIGABRT";
        case 8: return "SIGFPE";
        case 9: return "SIGKILL";
        case 11: return "SIGSEGV";
        case 13: return "SIGPIPE";
        case 14: return "SIGALRM";
        case 15: return "SIGTERM";
        // 7 and 10 are deliberately absent, they name different signals on
        // Linux (SIGBUS, SIGUSR1) than on macOS (SIGEMT, SIGBUS)
        default: return null;
        }
    }

    private void writeOutputToRedirection(String output, final String stdout) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(regrDirectory.toDirectory().getPath()+File.separator+stdout, StandardCharsets.ISO_8859_1));
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
            try (BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile, StandardCharsets.ISO_8859_1));
                    BufferedReader outputReader = new BufferedReader(new FileReader(outputFile, StandardCharsets.ISO_8859_1));) {
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
        boolean isSuspended = regrDirectory.hasSuspendedFile(caseName);
        if (fatal)
            return isSuspended ? State.SUSPENDED_FATAL : State.FATAL;
        if (!regrDirectory.hasExpectedFile(caseName) && !regrDirectory.hasOutputFile(caseName))
            return isSuspended ? State.SUSPENDED : State.VIRGIN;
        if (!regrDirectory.hasExpectedFile(caseName) && regrDirectory.hasOutputFile(caseName))
            return isSuspended ? State.SUSPENDED : State.PENDING;
        if (regrDirectory.hasExpectedFile(caseName) && !regrDirectory.hasOutputFile(caseName))
            return isSuspended ? State.SUSPENDED_PASS : State.PASS;
        return isSuspended ? State.SUSPENDED_FAIL : State.FAIL;
    }

    public boolean failed() {
        return status() == State.FAIL;
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
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(getOutputFile()), StandardCharsets.ISO_8859_1));
    }

}
