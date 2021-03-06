package se.alanif.jregr.exec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class StreamGobblerTest extends TestCase {

	@Before
	public void setUp() throws Exception {
		compile("stdout");
		compile("99bottles");
	}

	private void compile(String program) throws IOException, InterruptedException {
		// If on actual Windows, you need pre-compile the programs to pure Windows binaries, unless you have Cygwin...
		if (!System.getProperty("os.name").contains("Windows")) {
			Process p = Runtime.getRuntime().exec("cc -o " + program + " " + program + ".c", null, new File("unit"));
			p.waitFor();
		} else {
			Process p = Runtime.getRuntime().exec(new String[]{"C:\\cygwin64\\bin\\bash.exe", "-c", "x86_64-w64-mingw32-gcc -o " + program + " " + program + ".c"},
					new String[]{"PATH=/usr/bin"}, new File("unit"));
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
