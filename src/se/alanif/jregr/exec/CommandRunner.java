package se.alanif.jregr.exec;

public class CommandRunner {

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

	public String[] runCommandForOutput(String[] strings, ProcessBuilder mockedProcessBuilder) {
		return null;
	}

}
