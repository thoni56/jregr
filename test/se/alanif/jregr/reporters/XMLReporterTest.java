package se.alanif.jregr.reporters;

import junit.framework.TestCase;
import org.junit.Test;

public class XMLReporterTest extends TestCase {

	@Test
	public void testCanRemoveAnyControlCharacterFromStringWithoutAny() throws Exception {
		XMLReporter reporter = new XMLReporter();
		String inputString = "absdloeiruLJDLSJKDLHK";
		String outputString = reporter.removeControlCharactersFrom(inputString);

		assertEquals(inputString, outputString);
	}

	@Test
	public void testCanRemoveAnyControlCharacterFromStringWithBackspace() throws Exception {
		XMLReporter reporter = new XMLReporter();
		String inputString = "absdloeiruLJD\bSJKDLHK";
		String expectedString = "absdloeiruLJDSJKDLHK";
		String outputString = reporter.removeControlCharactersFrom(inputString);

		assertEquals(expectedString, outputString);
	}

}
