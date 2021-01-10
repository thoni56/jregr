package se.alanif.jregr.acceptance;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;


public class AcceptanceScenarios {

	private static final int STDOUT = 0;
	private static final int STDERR = 1;

	public static String[] runCommandForOutput(String[] arguments) {
		String[] jregr = {
				"java",
				"-cp", "bin;lib/commons-cli-1.4/*",
				"se.alanif.jregr.Main"
		};
		String[] allArguments = combine(jregr, arguments);
		ProcessBuilder pb = new ProcessBuilder(allArguments);
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
	public void shouldRecurseThroughEmptyDirectoryIntoSubdirectoryWithSingleTest() throws Exception {
		String directory = "one_subdir_with_a_case";
		String[] arguments = {
				"-dir", "acceptance/"+directory
		};
		String[] output = runCommandForOutput(arguments);
		if (output[1] != null)
			throw new Exception("Error message: "+output[1]);
		assertEquals("Running 1 tests in 'acceptance/"+directory+"/subdir"+"' :", output[STDOUT]);
	}
	
    private static String[] combine(String[] a, String[] b){
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}