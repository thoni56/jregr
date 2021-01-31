package se.alanif.jregr.io;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;

import org.junit.Test;

public class DirectoryTest {

	private static final String EXISTING_COMMAND_NAME_WITHOUT_EXE = "ThisCommandExistsWithoutExeExtensionOnAMac";
	private static final String EXISTING_COMMAND_NAME_WITH_EXE = "ThisCommandExistsButRequiresExeExtensionTypicallyForWindows";
	
	private Directory mockedDirectory = new Directory("someDirectory") {
		private static final long serialVersionUID = 1L;

		public boolean hasFile(String fileName) {
			return fileName.equals(EXISTING_COMMAND_NAME_WITHOUT_EXE) || fileName.equals(EXISTING_COMMAND_NAME_WITH_EXE+".exe");
		}
	};
	
	@Test
	public void testCanSeeIfExecutableWithoutExeExist() throws Exception {
		assertTrue(mockedDirectory.executableExist(EXISTING_COMMAND_NAME_WITHOUT_EXE));
	}

	@Test
	public void testCanSeeIfExecutableWithExeExist() throws Exception {
		assertTrue(mockedDirectory.executableExist(EXISTING_COMMAND_NAME_WITH_EXE));
	}
	
	@Test
	public void testCanSeeIfFileExists() throws Exception {
		Directory directory = new Directory("");
		java.io.File file = directory.getFile("file");
		file.createNewFile();
		
		assertTrue(directory.hasFile("file"));
		
		file.delete();
		assertTrue(!directory.hasFile("file"));
	}
	
	@Test
	public void testCanReturnBufferedReaderForFile() throws Exception {
		Directory directory = new Directory("");
		java.io.File file = directory.getFile("file");
		file.createNewFile();
		BufferedReader reader = directory.getBufferedReaderForFile(directory.getFile("file"));
		assertTrue(reader instanceof BufferedReader);
		reader.close();
		
		while (!file.delete())
			;
		reader = directory.getBufferedReaderForFile(directory.getFile("file"));
		assertNull(reader);
	}
	
}
