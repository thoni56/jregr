package se.alanif.jregr.exec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import se.alanif.jregr.io.Directory;

public class CommandRunner {

	public static final int STDOUT = 0;
	public static final int STDERR = 1;

	private StreamGobbler outputGobbler = null;
	private StreamGobbler errorGobbler = null;
	private StreamPusher inputPusher = null;
	private ProcessBuilderSpy processBuilder;

	// Setters for gobblers, input pusher and ProcessBuilder so we can inject mocks
	protected void setGobblers(StreamGobbler outputGobbler, StreamGobbler errorGobbler) {
		this.outputGobbler = outputGobbler;
		this.errorGobbler = errorGobbler;
	}
	protected void setInputPusher(StreamPusher inputPusher) {
		this.inputPusher = inputPusher;
	}
	public void setProcessBuilder(ProcessBuilderSpy processBuilder) {
		this.processBuilder = processBuilder;
	}

	public String runCommandForOutput(String[] commandAndArguments, String inputFilename, Directory directory) throws IOException, InterruptedException {
		if (processBuilder == null) processBuilder = new ProcessBuilderSpy();

		processBuilder.command(commandAndArguments);
		processBuilder.directory(directory);
		Process p = processBuilder.start();

		// No injected, possibly mocked, gobblers, create real ones
		if (outputGobbler == null)
			outputGobbler = new StreamGobbler(p.getInputStream());
		if (errorGobbler == null)
			errorGobbler = new StreamGobbler(p.getErrorStream());
		outputGobbler.start();
		errorGobbler.start();

		// Ditto for inputPusher
		if (inputFilename != null) {
			if (inputPusher == null) // No injected, possibly mocked, pusher? Create a real one!
				inputPusher  = new StreamPusher(p.getOutputStream(), new FileReader(directory.getPath()+File.separator+inputFilename));
			inputPusher.run();
		}

		p.waitFor();
		outputGobbler.join(); 
		errorGobbler.join(); 
		String output = outputGobbler.output() + errorGobbler.output();
		outputGobbler = null;
		errorGobbler = null;

		return output;
	}

}
