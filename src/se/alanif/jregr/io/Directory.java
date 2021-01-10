package se.alanif.jregr.io;

import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;

import se.alanif.jregr.CommandsDecoder;

public class Directory extends File {

	public Directory(String pathname) {
		super(pathname);
	}

	private static final long serialVersionUID = 1L;

	public boolean executableExist(String name) {
		return hasFile(name) || hasFile(name + ".exe");
	}

	public boolean hasFile(String fileName) {
		return getFile(fileName).exists();
	}

	public File getFile(String fileName) {
		return new File(getAbsolutePath() + separator + fileName);
	}

	public boolean executablesExist(CommandsDecoder decoder) {
		decoder.reset();
		do {
			if (hasFile(decoder.getCommand()) || hasFile(decoder.getCommand() + ".exe"))
				return true;
		} while (decoder.advance());
		return false;
	}

	public BufferedReader getBufferedReaderForFile(File file) {
		if (file.exists())
			try {
				return new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				return null;
			}
		else
			return null;
	}

	private FileFilter subDirectoryFilter = new FileFilter() {
		public boolean accept(java.io.File file) {
			return file.isDirectory();
		}
	};

	public Directory[] getSubdirectories() {
		java.io.File javaFiles[] = listFiles(subDirectoryFilter);
		Directory subdirectories[] = new Directory[javaFiles.length];
        for (int i = 0; i < javaFiles.length; i++)
            subdirectories[i] = new Directory(javaFiles[i].getPath());
		return subdirectories;
	}

}
