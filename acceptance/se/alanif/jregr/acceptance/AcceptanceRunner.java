package se.alanif.jregr.acceptance;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class AcceptanceRunner {

	public static final int STDOUT = 0;
	public static final int STDERR = 1;
	
	public static String[] runJregrForCleanOutput(String[] arguments) {
		String[] jregr = {
				"java",
				"-cp", "bin"+File.pathSeparator+"lib/commons-cli-1.4/*",
				"se.alanif.jregr.Main",
				"-noansi", "-nocolour"
		};
		String[] allArguments = combine(jregr, arguments);
		ProcessBuilder pb = new ProcessBuilder(allArguments);
		String[] result = new String[2];
		try {
			Process p = pb.start();

			final BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			result[STDOUT] = inputReader.lines().collect(Collectors.joining("\n"));

			final BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			result[STDERR] = errorReader.lines().collect(Collectors.joining("\n"));

			p.waitFor();
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

    private static String[] combine(String[] a, String[] b){
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
