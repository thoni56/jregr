package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import se.alanif.jregr.io.Directory;

public class CommandDecoder {

	// The RegrDecoder decodes a file (presumably a .jregr file) into
	// <extension> ':' <command> {<arg>} [ '<' <stdin> ]
	// It will expand $1 to the case name and $2 to the full filename
	// matched by the line (case name + extension)

	// It will work on one line at a time starting with the first
	// Use advance() to advance to next line
	// Feed it a BufferedReader for the .jregr file in the constructor

	// You need to set the caseName to use for expansion using reset(caseName)

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
	private String caseName = "";
	private boolean optional;

	public CommandDecoder(BufferedReader fileReader) throws IOException {
		jregrFileReader = fileReader;
		fileReader.mark(10000);
		readAndSplitLineIntoParts();
	}

	private void readAndSplitLineIntoParts() throws IOException {
		optional = false;
		String line = this.jregrFileReader.readLine();
		parts = splitIntoParts(line);
	}

	private String[] splitIntoParts(String line) throws CommandSyntaxException {
		if (line != null) {
			String[] parts = line.split(" ");
			if (parts.length < 2 || (!parts[1].equals(":") && !parts[1].equals("?"))) {
				throw new CommandSyntaxException("Syntax error: \""+line+"\"");
			}
			optional = parts[1].equals("?");
			parts = decodeStdinout(parts);
			return removeSeparatorInSecondPosition(parts);
		} else {
			return new String[] { "", "", "" };
		}
	}

	private String[] decodeStdinout(String[] parts) {
		stdinFilename = null; stdoutFilename = null;
		if (parts[parts.length - 2].equals("<")) {
			stdinFilename = parts[parts.length - 1];
			parts = Arrays.copyOf(parts, parts.length - 2);
		}
		if (parts[parts.length - 2].equals(">")) {
			stdoutFilename = parts[parts.length - 1];
			parts = Arrays.copyOf(parts, parts.length - 2);
		}
		if (parts[parts.length - 2].equals("<")) {
			stdinFilename = parts[parts.length - 1];
			parts = Arrays.copyOf(parts, parts.length - 2);
		}
		return parts;
	}

	private String[] removeSeparatorInSecondPosition(String[] split) {
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
		if (parts.length > 1)
			return parts[1];
		else
			return null;
	}

	private String[] getArguments() {
		if (parts.length > 1) {
			String[] arguments = new String[parts.length - 2];
			for (int i = 2; i < parts.length; i++)
				arguments[i - 2] = parts[i];
			return arguments;
		} else
			return new String[]{""};
	}

	public String getExtension() {
		return parts[0];
	}

	public String getStdin() {
		String r;
		r = stdinFilename;
		if (r != null)
			return expandSymbols(caseName, r);
		else
			return r;
	}

	public String getStdout() {
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
		if (command != null) {
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
		} else
			return null;
	}

	public boolean advance() throws CommandSyntaxException {
		String line = "";
		try {
			line = jregrFileReader.readLine();
		} catch (IOException e) {
			return false;
		}
		if (line == null || line.equals(""))
			return false;
		else
			parts = splitIntoParts(line);
		return true;
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

	public boolean isOptional() {
		return optional;
	}

}
