package se.alanif.jregr.exec;

import java.io.OutputStream;

import se.alanif.jregr.exec.StreamGobbler;

import junit.framework.TestCase;

public class StreamGobblerTest extends TestCase {
	
	public void testCanGobble1000Lines() throws Exception {
		Process p = Runtime.getRuntime().exec("test/stdout"); // This program has to be a native Windows program, cygwin doesn't work!!!
		StreamGobbler gobbler = new StreamGobbler(p.getInputStream());
		
		gobbler.start();
		p.waitFor();
		gobbler.join();

		String input = gobbler.output();
		int count = input.split("\n").length;
		assertEquals(1000, count);
	}
	
	public void testCanGobbleAllOf99BottlesAndSendInput() throws Exception {
		Process p = Runtime.getRuntime().exec("test/99bottles");

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
