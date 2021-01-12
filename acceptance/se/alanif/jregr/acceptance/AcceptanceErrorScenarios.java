package se.alanif.jregr.acceptance;

import static org.junit.Assert.assertEquals;
import static se.alanif.jregr.acceptance.AcceptanceRunner.STDERR;
import static se.alanif.jregr.acceptance.AcceptanceRunner.STDOUT;
import static se.alanif.jregr.acceptance.AcceptanceRunner.runJregrForCleanOutput;

import org.junit.Test;

public class AcceptanceErrorScenarios {


	@Test
	public void shouldRequireJregrFileInImplicitTopDirectory() throws Exception {
		String[] output = runJregrForCleanOutput(new String[0]);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory 'Jregr' - top level directory must have a non-empty .jregr file", output[STDOUT]);
	}

	@Test
	public void shouldRequireJregrFileInExplicitTopDirectory() throws Exception {
		String directory = "nojregr";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory '"+directory+"' - top level directory must have a non-empty .jregr file", output[0]);
	}
	
	
	@Test
	public void shouldSignalErrorForJregrFileWithEmptyLine() throws Exception {
		String directory = "malformed_jregr_empty_line";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory '"+directory+"' - syntax error in .jregr file", output[0]);
	}
	

	@Test
	public void shouldSignalErrorForJregrFileWithOnlyExtension() throws Exception {
		String directory = "malformed_jregr_only_extension";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory '"+directory+"' - syntax error in .jregr file", output[0]);
	}
	
	@Test
	public void shouldSignalErrorForJregrFileWithNoCommand() throws Exception {
		String directory = "malformed_jregr_without_command";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory '"+directory+"' - syntax error in .jregr file", output[0]);
	}
	
	@Test
	public void shouldSignalErrorForJregrFileWithNoSeparatedColon() throws Exception {
		String directory = "malformed_jregr_with_no_separated_colon";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runJregrForCleanOutput(arguments);
		assertEquals("", output[STDERR]);
		assertEquals("Error: Directory '"+directory+"' - syntax error in .jregr file", output[0]);
	}
	
}