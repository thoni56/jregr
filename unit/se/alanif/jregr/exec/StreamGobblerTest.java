package se.alanif.jregr.exec;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class StreamGobblerTest extends TestCase {

	@Before
	public void setUp() throws Exception {
		compile("stdout");
		compile("99bottles");
	}

	private void compile(String program) throws IOException, InterruptedException {
		// If on Windows, you need pre-compile the programs to pure Windows binaries, not cygwin or similar
		if (!System.getProperty("os.name").contains("Windows")) {
			Process p = Runtime.getRuntime().exec("cc -o " + program + " " + program + ".c", null, new File("unit"));
			p.waitFor();
		}
	}

	@Test
	public void testCanGobble1000Lines() throws Exception {
		Process p = Runtime.getRuntime().exec("unit/stdout"); 	// If on Windows, this program has to be a native Windows program
																// since it execs a Windows process, so cygwin programs don't work
		StreamGobbler gobbler = new StreamGobbler(p.getInputStream());

		gobbler.start();
		p.waitFor();
		gobbler.join();

		String input = gobbler.output();
		int count = input.split("\n").length;
		assertEquals(1000, count);
	}

	@Test
	public void testCanGobbleAllOf99BottlesAndSendInput() throws Exception {
		Process p = Runtime.getRuntime().exec("unit/99bottles");

		OutputStream outputStream = p.getOutputStream();
		outputStream.write('\n');
		outputStream.flush();

		StreamGobbler gobbler = new StreamGobbler(p.getInputStream());

		gobbler.start();
		p.waitFor();
		gobbler.join();

		String input = gobbler.output();
		int count = input.split("\n").length;
		assertEquals(499, count);
	}
}
