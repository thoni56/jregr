package se.alanif.jregr.acceptance;

import static org.junit.Assert.assertEquals;
import static se.alanif.jregr.acceptance.AcceptanceRunner.*;

import org.junit.Test;


public class AcceptanceScenarios {

	@Test
	public void shouldRecurseThroughEmptyDirectoryIntoSubdirectoryWithSingleTest() throws Exception {
		String directory = "one_subdir_with_a_case";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runCommandForOutput(arguments);
		assertEquals(output[STDERR], "");
		String[] outputLines = output[STDOUT].split("\n");
		assertEquals("Running 0 test(s) in 'acceptance/"+directory+"' :", outputLines[0]);
		assertEquals("Running 1 test(s) in 'acceptance/"+directory+"/subdir"+"' :", outputLines[1]);
	}
	
}