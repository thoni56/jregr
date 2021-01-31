package se.alanif.jregr.io;

import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;

import se.alanif.jregr.CommandDecoder;

public class Directory extends File {

	public Directory(String path) {
		super(path);
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
	
	public String[] getFilenamesWithExtension(String extension) {
		String[] fileNames = list(new FilenameFilter() {
			
			@Override
			public boolean accept(java.io.File dir, String name) {
				return name.endsWith(extension);
			}
		});
		return fileNames;
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

	public boolean exists(String filename) {
		return new java.io.File(getPath(), filename).isFile();
	}

}
