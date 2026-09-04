package se.alanif.jregr.acceptance;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AcceptanceRunner {

	public static final int STDOUT = 0;
	public static final int STDERR = 1;
	
	/* What a run of jregr amounted to. The exit code is here because a
	   scenario about whether a suite failed can only be written if the
	   harness can see it -- runJregrForCleanOutput threw away the value of
	   waitFor(), which is the same mistake as the one the subdirectory
	   scenario below was written to catch. */
	public record Run(String stdout, String stderr, int exitCode) {
	}

	public static Run runJregr(String[] arguments) {
		String[] jregr = {
				"java",
				"-cp", "bin"+File.pathSeparator+"lib/commons-cli-1.4/*",
				"se.alanif.jregr.Main",
				"-noansi", "-nocolour"
		};
		String[] allArguments = combine(jregr, arguments);
		ProcessBuilder pb = new ProcessBuilder(allArguments);
		String stdout = "";
		String stderr = "";
		int exitCode = -1;
		try {
			Process p = pb.start();

			final BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.ISO_8859_1));
			stdout = outputReader.lines().collect(Collectors.joining("\n"));

			final BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.ISO_8859_1));
			stderr = errorReader.lines().collect(Collectors.joining("\n"));

			exitCode = p.waitFor();
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Run(stdout, stderr, exitCode);
	}

	public static String[] runJregrForCleanOutput(String[] arguments) {
		Run run = runJregr(arguments);
		String[] result = new String[2];
		result[STDOUT] = run.stdout();
		result[STDERR] = run.stderr();
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
