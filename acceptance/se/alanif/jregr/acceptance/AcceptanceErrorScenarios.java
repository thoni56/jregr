package se.alanif.jregr.acceptance;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;


public class AcceptanceErrorScenarios {
	
	private static final int STDOUT = 0;
	private static final int STDERR = 1;

	public static String[] runCommandForOutput(String[] params) {
		ProcessBuilder pb = new ProcessBuilder(params);
		Process p = null;
		String[] result = new String[2];
		try {
			p = pb.start();
			final BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			result[STDOUT] = inputReader.readLine();
			final BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			result[STDERR] = errorReader.readLine();

			p.waitFor();
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Test
	public void shouldRequireJregrFileInImplicitTopDirectory() throws Exception {
		String[] arguments = {
				"java.exe",
				"-cp", "bin;lib/commons-cli-1.4/*",
				"se.alanif.jregr.Main"
		};
		String[] output = runCommandForOutput(arguments);
		if (output[STDERR] != null)
			throw new Exception("Error message: "+output[STDERR]);
		assertEquals("Error: Directory 'Jregr' - top level directory must have .jregr file that is non-empty", output[STDOUT]);
	}

	@Test
	public void shouldRequireJregrFileInExplicitTopDirectory() throws Exception {
		String[] arguments = {
				"java.exe",
				"-cp", "bin;lib/commons-cli-1.4/*",
				"se.alanif.jregr.Main",
				"-dir", "acceptance/nojregr"
		};
		String[] output = runCommandForOutput(arguments);
		if (output[1] != null)
			throw new Exception("Error message: "+output[1]);
		assertEquals("Error: Directory 'nojregr' - top level directory must have .jregr file that is non-empty", output[0]);
	}

}
