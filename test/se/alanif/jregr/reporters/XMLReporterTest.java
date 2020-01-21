package se.alanif.jregr.reporters;

import junit.framework.TestCase;
import org.junit.Test;

public class XMLReporterTest extends TestCase {

	@Test
	public void testWillReturnSameStringWithoutAnyControlCharacters() throws Exception {
		XMLReporter reporter = new XMLReporter();
		String inputString = "absdloeiruLJDLSJKDLHK";
		String outputString = reporter.removeControlCharactersFrom(inputString);

		assertEquals(inputString, outputString);
	}

	@Test
	public void testWillRemoveBackspaceFromStringWithBackspaceAndNewLine() throws Exception {
		XMLReporter reporter = new XMLReporter();
		String inputString = "absd\nloeiruLJD\bSJKDLHK";
		String expectedString = "absd\nloeiruLJDSJKDLHK";
		String outputString = reporter.removeControlCharactersFrom(inputString);

		assertEquals(expectedString, outputString);
	}

}
