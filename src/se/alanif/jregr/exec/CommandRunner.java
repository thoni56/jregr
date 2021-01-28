package se.alanif.jregr.exec;

import java.io.FileReader;
import java.io.IOException;

public class CommandRunner {

	public static final int STDOUT = 0;
	public static final int STDERR = 1;

	private StreamGobbler outputGobbler = null;
	private StreamGobbler errorGobbler = null;
	private StreamPusher inputPusher = null;
	private ProcessBuilderSpy processBuilder;

	// Set gobblers to be able to inject mocks
	protected void setGobblers(StreamGobbler outputGobbler, StreamGobbler errorGobbler) {
		this.outputGobbler = outputGobbler;
		this.errorGobbler = errorGobbler;
	}

	protected void setInputPusher(StreamPusher inputPusher) {
		this.inputPusher = inputPusher;
	}

	public String run(Process p, StreamGobbler errorGobbler, StreamGobbler outputGobbler, StreamPusher inputPusher) {
		errorGobbler.start();
		outputGobbler.start();

		if (inputPusher != null) {
			inputPusher.start();
		}

		try {
			p.waitFor();
			errorGobbler.join();
			outputGobbler.join();
		} catch (InterruptedException e) {
		}
		return outputGobbler.output() + errorGobbler.output();
	}

	public String runCommandForOutput(String[] commandAndArguments, String inputFilename) {
		if (processBuilder == null) processBuilder = new ProcessBuilderSpy();

		processBuilder.command(commandAndArguments);
		try {
			Process p = processBuilder.start();
			if (outputGobbler == null) {
				// No injected, possibly mocked, gobblers, create real ones
				outputGobbler = new StreamGobbler(p.getInputStream());
				errorGobbler = new StreamGobbler(p.getErrorStream());
			}
			outputGobbler.start();
			errorGobbler.start();

			if (inputFilename != null) {
				if (inputPusher == null) // No injected, possibly mocked, pusher? Create a real one!
					inputPusher  = new StreamPusher(p.getOutputStream(), new FileReader(inputFilename));
				inputPusher.run();
			}

			p.waitFor();
			errorGobbler.join();
			outputGobbler.join();

			return outputGobbler.output() + errorGobbler.output();
		} catch (InterruptedException e) {
			return "*** InterruptedException in Jregr ***";
		} catch (IOException e) {
			return "*** IOException in Jregr ***";
		}
	}


	public void setProcessBuilder(ProcessBuilderSpy processBuilder) {
		this.processBuilder = processBuilder;
	}

}
