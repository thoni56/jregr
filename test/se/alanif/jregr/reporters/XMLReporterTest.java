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

}
