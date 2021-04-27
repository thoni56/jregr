package se.alanif.jregr.reporters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.diff.Diff;
import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

// TODO XMLReporter should really use some XML library like JDOM

public class XMLReporter extends AbstractRegrReporter {

    private static final String CONTROL_CHARACTER_REGEX = "[\\p{Cntrl}&&[^\r\n\t]]";
    private String suiteName;
    private RegrCase theCase;
    private PrintStream xmlOutput = System.out;
    private long millis;

    public XMLReporter(Directory regrDirectory) throws FileNotFoundException {
        File xmlFile = regrDirectory.getFile("TEST-jregr.xml");
        OutputStream xmlStream = new FileOutputStream(xmlFile);
        xmlOutput = new PrintStream(xmlStream);
    }

    // No arguments constructor for tests
    public XMLReporter() {
    }

    public void start(CommandLine commandLine) {
        xmlOutput.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
    }

    public void startSuite(String suiteName, int numberOfTests) {
        this.suiteName = suiteName;
        xmlOutput.println("<testsuite name=\"" + suiteName + "\">");
    }

    public void startTest(RegrCase caseName, long millis) {
        this.theCase = caseName;
        this.millis = millis;
    }

    public void fatal() {
        fatal++;
        header(theCase.getName(), millis);
        xmlOutput.println("    <error type=\"Fatal\" message=\"Case '" + theCase + "' failed to complete\">");
        xmlOutput.println("    </error>");
        tail();
    }

    public void virgin() {
        virgin++;
        header(theCase.getName(), millis);
        xmlOutput.println("    <error type=\"Virgin\" message=\"No input defined for case '" + theCase + "'\">");
        xmlOutput.println("      The file '" + theCase + ".input' does not exist");
        xmlOutput.println("    </error>");
        tail();
    }

    public void pending() {
        pending++;
        header(theCase.getName(), millis);
        xmlOutput.println(
                "    <error type=\"Pending\" message=\"No expected output defined for case '" + theCase + "'\">");
        xmlOutput.println("      The file '" + theCase + ".expected' does not exist");
        xmlOutput.println("    </error>");
        tail();
    }

    public void fail() {
        failing++;
        header(theCase.getName(), millis);
        xmlOutput.println("    <failure message=\"Output does not match expected\">");
        insertDiff(theCase, xmlOutput);
        xmlOutput.println("    </failure>");
        tail();
    }

    private static String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff" + "]";

    private void insertDiff(RegrCase theCase, PrintStream outputStream) {
        xmlOutput.println("        <![CDATA[Compared to the expected output, the actual has");
        File outputFile = theCase.getOutputFile();
        String output = outputFile.getContent();
        if (output.matches(CONTROL_CHARACTER_REGEX)) {
            // Need to create a new file because Diff API is (File, File)
            java.io.File tempFile = null;
            try {
                output = output.replaceAll(xml10pattern, "");
                tempFile = File.createTempFile("jregr", "output");
            } catch (IOException e) {
                xmlOutput.println("FATAL - Could not create temporary file");
            }
            new Diff(xmlOutput).doDiff(theCase.getExpectedFile(), tempFile);
        } else
            new Diff(xmlOutput).doDiff(theCase.getExpectedFile(), theCase.getOutputFile());
        outputStream.println("]]>");
    }

    public void pass() {
        passing++;
        header(theCase.getName(), millis);
        insertExpectedOutput(theCase, xmlOutput);
        tail();
    }

    private void insertExpectedOutput(RegrCase theCase, PrintStream outputStream) {
        outputStream.println("        <![CDATA[");
        try {
            BufferedReader expectedReader = new BufferedReader(new FileReader(theCase.getExpectedFile()));
            String line = "";
            while ((line = expectedReader.readLine()) != null)
                outputStream.println(line);
            expectedReader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        outputStream.println("]]>");
    }

    public void suspended() {
        suspended++;
        header(theCase.getName(), 0);
        xmlOutput.println("    <skipped />");
        tail();
    }

    public void suspendedAndFailed() {
        suspended();
    }

    public void suspendedAndPassed() {
        suspended();
    }

    public void endSuite() {
        xmlOutput.println("</testsuite>");
    }

    private void header(String caseName, long millis) {
        xmlOutput.println("  <testcase classname=\"" + suiteName + "\" name=\"" + caseName + "\" time=\""
                + (float) millis / 1000 + "\">");
    }

    private void tail() {
        xmlOutput.println("  </testcase>");
    }

    public String removeControlCharactersFrom(String inputString) {
        return inputString.replaceAll(CONTROL_CHARACTER_REGEX, "");
    }

    public void end() {
    }

}
