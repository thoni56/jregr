package se.alanif.jregr;

import junit.framework.TestCase;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

public class OptionManagerTest extends TestCase {

	public void testCanParseBooleanGuiOption() throws Exception {
		OptionsManager optionManager = new OptionsManager();
		String[] args = new String[]{"-gui"};
		CommandLine commandLine = new BasicParser().parse(optionManager, args);
		assertTrue(commandLine.hasOption("gui"));
	}
	
	public void testCanParseBinDirectoryOption() throws Exception {
		OptionsManager optionManager = new OptionsManager();
		String[] args = new String[]{"-bin=some/dir"};
		CommandLine commandLine = new GnuParser().parse(optionManager, args);
		assertTrue(commandLine.hasOption("bin"));
		assertEquals("some/dir", commandLine.getOptionValue("bin"));
	}
}
