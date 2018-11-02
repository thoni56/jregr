package se.alanif.jregr.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class File extends java.io.File {

	private static final long serialVersionUID = 1L;

	public File(String pathname) {
		super(pathname);
	}

	public String getContent() {
		String content = "";

		try {
			BufferedReader input = new BufferedReader(
										new InputStreamReader(
												new FileInputStream(this), StandardCharsets.ISO_8859_1));
			try {
				int ch;
				while ((ch = input.read()) != -1) {
					if (ch != '\r')
						content += String.valueOf((char)ch);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return content;
	}

}
