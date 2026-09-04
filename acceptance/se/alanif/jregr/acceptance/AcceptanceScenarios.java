package se.alanif.jregr.acceptance;

import static org.junit.Assert.*;
import static se.alanif.jregr.acceptance.AcceptanceRunner.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;


public class AcceptanceScenarios {

    @Before
    public void setUp() throws Exception {
        compile("theSUT");
        compile("crash");
        compile("stderr");
        compile("bytes");
    }

    private void compile(String program) throws IOException, InterruptedException {
        // If on actual Windows, you need pre-compile the programs to pure Windows binaries, unless you have Cygwin...
        Process p;
        if (!System.getProperty("os.name").contains("Windows")) {
            p = Runtime.getRuntime().exec("cc -o " + program + " " + program + ".c", null, new File("acceptance"));
        } else {
            p = Runtime.getRuntime().exec(new String[]{"C:\\cygwin64\\bin\\bash.exe", "-c", "x86_64-w64-mingw32-gcc -o " + program + " " + program + ".c"},
                    new String[]{"PATH=/usr/bin"}, new File("acceptance"));
        }
        // Drain the diagnostics before waiting, then insist the compile
        // worked. A silently failed compile used to surface much later, as a
        // missing binary and a misleading Fatal
        String diagnostics = contentsOfStream(p.getErrorStream());
        assertEquals("could not compile " + program + ".c:\n" + diagnostics, 0, p.waitFor());
    }

    private String contentsOfStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }

    @Test
    public void shouldRunSingleTestInExplicitDirectory() throws Exception {
        String directory = "one_case";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"': Running 1 test(s)...", outputLines[0]);
        assertEquals("one : Pass", outputLines[1]);
    }

    @Test
    public void shouldNotRecurseIntoSubdirectoryWithoutJregr() throws Exception {
        String directory = "one_empty_subdir";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"': Running 0 test(s)...", outputLines[0]);
        assertEquals("'"+directory+"': ran 0 test(s)", outputLines[1]);
    }

    @Test
    public void shouldRecurseThroughEmptyDirectoryWithJregrFileIntoSubdirectoryWithSingleTest() throws Exception {
        String directory = "one_subdir_with_a_case";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"/subdir': Running 1 test(s)...", outputLines[2]);
    }

    @Test
    public void shouldUseSameJregrInSubdirectoryWithEmptyJregr() throws Exception {
        String directory = "one_subdir_with_empty_jregr";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"/subdir"+"': Running 1 test(s)...", outputLines[2]);
        assertEquals("a_case_in_subdir : Pass", outputLines[3]);
    }

    @Test
    public void shouldRunInSubdirectoryWithDifferentJregr() throws Exception {
        String directory = "one_subdir_with_different_jregr";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"/subdir"+"': Running 1 test(s)...", outputLines[2]);
        assertEquals("a_case_in_subdir_with_different_jregr : Pass", outputLines[3]);
    }

    @Test
    public void shouldCreateRedirectedOutputInCaseDirectory() {
        String directory = "one_subdir_with_redirected_output";
        String[] arguments = {
                "-dir", "acceptance/"+directory,
                "-bin", "acceptance"
        };

        // Ensure the redirected output file does not exist
        String redirectedOutputFilename = "acceptance"+File.separator+directory+File.separator+"subdir/a_case_in_subdir.out";
        File stdoutFile = new File(redirectedOutputFilename);
        stdoutFile.delete();

        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"/subdir"+"': Running 1 test(s)...", outputLines[2]);
        assertEquals("a_case_in_subdir : Pass", outputLines[3]);

        // Assert it does
        assertTrue(stdoutFile.exists());
    }

    @Test
    public void shouldFindExeWithRelativePathInJregr() throws Exception {
        String directory = "one_subdir_with_relative_path_to_exe";
        String[] arguments = {
                "-dir", "acceptance/"+directory
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"/subdir"+"': Running 1 test(s)...", outputLines[2]);
        assertEquals("a_case_in_subdir_with_relative_path_to_exe : Pass", outputLines[3]);
    }

    @Test
    public void shouldCatchStderr() {
        String directory = "should_catch_stderr";
        String[] arguments = {
                "-dir", "acceptance/"+directory
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"': Running 1 test(s)...", outputLines[0]);
        assertEquals("should_catch_stderr : Pass", outputLines[1]);
    }

    @Test
    public void shouldRunTwoCasesInSubdirectory() {
        String directory = "one_subdir_with_two_cases";
        String[] arguments = {
                "-dir", "acceptance/"+directory
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'one_subdir_with_two_cases': Running 0 test(s)...", outputLines[0]);
        assertEquals("'one_subdir_with_two_cases/subdir': Running 2 test(s)...", outputLines[2]);

    }

    @Test
    public void shouldStopExecutingCommandsAfterACrash() throws Exception {
        String directory = "crash_should_stop_execution";
        String[] arguments = {
                "-dir", "acceptance/"+directory
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals(output[STDERR], "");
        String[] outputLines = output[STDOUT].split("\n");
        assertEquals("'"+directory+"': Running 1 test(s)...", outputLines[0]);
        assertEquals("crash_should_stop_execution : Fatal", outputLines[1]);

        // The point of the case: the crash must be reported, and the
        // second line of the .jregr file must never have run
        String caseOutput = contentsOf(directory+File.separator+"crash_should_stop_execution.output");
        assertTrue(caseOutput, caseOutput.contains("terminated by signal"));
        assertFalse(caseOutput, caseOutput.contains("does not exist"));
    }

    private String contentsOf(String filenameRelativeToAcceptance) throws IOException {
        return new String(Files.readAllBytes(Paths.get("acceptance", filenameRelativeToAcceptance)));
    }

    // Character sets. Jregr compares what a program emitted, so bytes have
    // to pass through it untouched. These pin that down at the byte level,
    // because a jregr that transcodes can still look correct from the
    // verdict alone: two different bytes both decode to U+FFFD and then
    // compare equal.

    @Test
    public void shouldPreserveARawLatin1ByteThroughTheRoundTrip() throws Exception {
        String[] outputLines = runCharacterSetCase("charset_latin1_roundtrip", "latin1");
        assertEquals("latin1 : Pass", outputLines[1]);
    }

    @Test
    public void shouldPassAUtf8SequenceThroughUntouched() throws Exception {
        // What Alan's suites actually contain
        String[] outputLines = runCharacterSetCase("charset_utf8_sequence", "utf8");
        assertEquals("utf8 : Pass", outputLines[1]);
    }

    @Test
    public void shouldNotConsiderTwoDifferentBytesEqual() throws Exception {
        // The case emits 0xE5 where .expected holds 0xE4. Read as UTF-8
        // both become the replacement character, so a jregr that decodes
        // calls this a Pass -- a false positive on a real difference.
        String[] outputLines = runCharacterSetCase("charset_byte_mismatch", "mismatch");
        assertEquals("mismatch : Fail", outputLines[1]);
        assertArrayEquals(headerAnd("mismatch", 0xE5, 0x0A),
                          outputBytes("charset_byte_mismatch", "mismatch"));
    }

    @Test
    public void shouldPushInputBytesIntoTheSutUnchanged() throws Exception {
        // The .expected deliberately does not match, so that the .output
        // survives to be read. The verdict is not the point here, the bytes
        // in the .output are: they came in through the case file, through
        // the SUT's stdin, and back out again.
        String[] outputLines = runCharacterSetCase("charset_stdin_bytes", "stdin");
        assertEquals("stdin : Fail", outputLines[1]);
        assertArrayEquals(headerAnd("stdin", 0xE4, 0x0A),
                          outputBytes("charset_stdin_bytes", "stdin"));
    }

    private String[] runCharacterSetCase(String directory, String caseName) throws Exception {
        // A stale .output from an earlier run would be compared instead of
        // a fresh one, so start from nothing
        Files.deleteIfExists(outputPath(directory, caseName));
        String[] arguments = {
                "-dir", "acceptance/"+directory
        };
        String[] output = runJregrForCleanOutput(arguments);
        assertEquals("", output[STDERR]);
        return output[STDOUT].split("\n");
    }

    private Path outputPath(String directory, String caseName) {
        return Paths.get("acceptance", directory, caseName+".output");
    }

    private byte[] outputBytes(String directory, String caseName) throws IOException {
        return Files.readAllBytes(outputPath(directory, caseName));
    }

    private byte[] headerAnd(String caseName, int... trailing) {
        byte[] header = ("########## "+caseName+" ##########\n").getBytes(StandardCharsets.US_ASCII);
        byte[] all = Arrays.copyOf(header, header.length + trailing.length);
        for (int i = 0; i < trailing.length; i++)
            all[header.length + i] = (byte) trailing[i];
        return all;
    }
}
