package se.alanif.jregr.acceptance;

import static org.junit.Assert.*;
import static se.alanif.jregr.acceptance.AcceptanceRunner.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


public class AcceptanceScenarios {
	
	@Before
	public void setUp() throws Exception {
		compile("theSUT");
	}

	private void compile(String program) throws IOException, InterruptedException {
		// If on actual Windows, you need pre-compile the programs to pure Windows binaries, unless you have Cygwin...
		if (!System.getProperty("os.name").contains("Windows")) {
			Process p = Runtime.getRuntime().exec("cc -o " + program + " " + program + ".c", null, new File("acceptance"));
			p.waitFor();
		} else {
			Process p = Runtime.getRuntime().exec(new String[]{"C:\\cygwin64\\bin\\bash.exe", "-c", "x86_64-w64-mingw32-gcc -o " + program + " " + program + ".c"},
					new String[]{"PATH=/usr/bin"}, new File("acceptance"));
			p.waitFor();
		}
	}

	@Test
	public void shouldRunSingleTestInExplicitDirectory() throws Exception {
		String directory = "one_case";
		String[] arguments = {
				"-dir", "acceptance/"+directory,
				"-bin", "acceptance",
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
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals(output[STDERR], "");
		String[] outputLines = output[STDOUT].split("\n");
		assertEquals("'"+directory+"': Running 0 test(s)...", outputLines[0]);
		assertEquals("'"+directory+"': ran 0 test(s)", outputLines[1]);
	}
	
	@Test
	public void shouldRecurseThroughEmptyDirectoryIntoSubdirectoryWithSingleTest() throws Exception {
		String directory = "one_subdir_with_a_case";
		String[] arguments = {
				"-dir", "acceptance/"+directory,
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
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals(output[STDERR], "");
		String[] outputLines = output[STDOUT].split("\n");
		assertEquals("'"+directory+"/subdir"+"': Running 1 test(s)...", outputLines[2]);
		assertEquals("a_case_in_subdir : Pass", outputLines[3]);
	}
	
	@Test
	public void shouldCreateRedirectedOutputInCaseDirectory() {
		String directory = "one_subdir_with_redirected_output";
		String[] arguments = {
				"-dir", "acceptance/"+directory,
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
}