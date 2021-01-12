package se.alanif.jregr;

import junit.framework.TestCase;
import org.junit.Test;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;

public class OptionManagerTest extends TestCase {

	@Test
	public void testCanParseBinDirectoryOption() throws Exception {
		OptionsManager optionManager = new OptionsManager();
		String[] args = new String[] { "-bin=some/dir" };
		CommandLine commandLine = new DefaultParser().parse(optionManager, args);
		assertTrue(commandLine.hasOption("bin"));
		assertEquals("some/dir", commandLine.getOptionValue("bin"));
	}
}
