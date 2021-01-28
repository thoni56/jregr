package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import se.alanif.jregr.io.Directory;

public class CommandsDecoder {

	// The RegrDecoder decodes a file (presumably a .jregr file) into
	// <extension> ':' <command> {<arg>} [ '<' <stdin> ]
	// It will expand $1 to the case name and $2 to the full filename
	// matched by the line (case name + extension)

	// It will work on one line at a time starting with the first
	// Use advance() to advance to next line
	// Feed it a BufferedReader in the constructor

	// It also handles the case where there is no .jregr file
	// and then serves the standard commands ".alan : alan $1" + ".a3c : arun $1 <
	// $1.input"
	
	@SuppressWarnings("serial")
	public class CommandSyntaxException extends IOException {
		public CommandSyntaxException(String errorMessage) {
	        super(errorMessage);
	    }
	}

	private String[] parts;
	private BufferedReader jregrFileReader;
	private String stdinFilename;
	private String stdoutFilename;
	private String caseName;

	public CommandsDecoder(BufferedReader fileReader) throws IOException {
		jregrFileReader = fileReader;
		fileReader.mark(10000);
		readAndSplitLineIntoParts();
	}

	private void readAndSplitLineIntoParts() throws IOException {
		String line = this.jregrFileReader.readLine();
		if (line != null)
			parts = splitIntoParts(line);
		else
			parts = new String[] { "", "", "" };
	}

	private String[] splitIntoParts(String line) throws CommandSyntaxException {
		String[] split = line.split(" ");
		if (split.length < 3 || !split[1].equals(":")) {
			throw new CommandSyntaxException("Syntax error in .jregr file");
		}
		split = decodeStdinout(split);
		return removeColonInSecondPosition(split);
	}

	private String[] decodeStdinout(String[] split) {
		stdinFilename = null; stdoutFilename = null;
		if (split[split.length - 2].equals("<")) {
			stdinFilename = split[split.length - 1];
			split = Arrays.copyOf(split, split.length - 2);
		}
		if (split[split.length - 2].equals(">")) {
			stdoutFilename = split[split.length - 1];
			split = Arrays.copyOf(split, split.length - 2);
		}
		if (split[split.length - 2].equals("<")) {
			stdinFilename = split[split.length - 1];
			split = Arrays.copyOf(split, split.length - 2);
		}
		return split;
	}

	private String[] removeColonInSecondPosition(String[] split) {
		String[] w = Arrays.copyOf(split, split.length - 1);
		for (int i = 1; i < w.length; i++)
			w[i] = split[i + 1];
		return w;
	}

	private String expandSymbols(String caseName, String template) {
		template = template.replace("$1", caseName);
		template = template.replace("$2", caseName + getExtension());
		return template;
	}

	public String getCommand() {
		return parts[1];
	}

	private String[] getArguments() {
		String[] arguments = new String[parts.length - 2];
		for (int i = 2; i < parts.length; i++)
			arguments[i - 2] = parts[i];
		return arguments;
	}

	public String getExtension() {
		return parts[0];
	}

	public String getStdin(String caseName) {
		String r;
		r = stdinFilename;
		if (r != null)
			return expandSymbols(caseName, r);
		else
			return r;
	}

	public String getStdout(String caseName) {
		String r;
		r = stdoutFilename;
		if (r != null)
			return expandSymbols(caseName, r);
		else
			return r;
	}

	public String[] buildCommandAndArguments(Directory binDirectory, String caseName) {
		final String binPath = binDirectory != null ? binDirectory.getAbsolutePath() + java.io.File.separator : "";
		String command = getCommand();
		if (binDirectory == null || !binDirectory.executableExist(command))
			command = expandSymbols(caseName, command);
		else
			command = binPath + expandSymbols(caseName, command);
		String[] arguments = getArguments();
		String[] commandAndArguments = new String[arguments.length + 1];
		commandAndArguments[0] = command;
		for (int i = 0; i < arguments.length; i++) {
			commandAndArguments[i + 1] = expandSymbols(caseName, arguments[i]);;
		}
		return commandAndArguments;
	}

	public boolean advance() {
		try {
			final String line = jregrFileReader.readLine();
			if (line == null || line.equals(""))
				return false;
			else
				parts = splitIntoParts(line);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void reset(String caseName) {
		this.caseName = caseName;
		reset();
	}

	public void reset() {
		try {
			jregrFileReader.reset();
			readAndSplitLineIntoParts();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
