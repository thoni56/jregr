package se.alanif.jregr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

public class RegrDirectoryTest {

	private static final String EXTENSION = ".extension";

	private static final String CASENAME1 = "case1";
	private static final String CASENAME2 = "case2";

	private static final String FILENAME1 = CASENAME1+EXTENSION;
	private static final String FILENAME2 = CASENAME2+EXTENSION;

	private static final String[] NO_FILENAMES = new String[] {};
	private static final String[] ONE_FILENAME_MATCHING_EXTENSION = new String[] { FILENAME1 };
	private static final String[] TWO_FILENAMES_MATCHING_EXTENSION = new String[] { FILENAME1, FILENAME2 };

	private final Directory mockedDirectoryWithoutCommandsFile = mock(Directory.class);
	private final Directory mockedDirectoryWithCommandsFile = mock(Directory.class);

	private final File mockedFile = new File("mockedFile");

	private RegrDirectory regrDirectoryWithoutCommandsFile;
	private RegrDirectory regrDirectoryWithCommandsFile;

	private File jregrFile = new File(RegrDirectory.COMMANDS_FILE_NAME);

	private static final RegrCase[] NO_CASES = new RegrCase[] {};

	@Before
	public void setUp() throws Exception {
		mockedFile.deleteOnExit();

		when(mockedDirectoryWithCommandsFile.hasFile(RegrDirectory.COMMANDS_FILE_NAME)).thenReturn(true);
		when(mockedDirectoryWithCommandsFile.getFile(RegrDirectory.COMMANDS_FILE_NAME)).thenReturn(jregrFile);
		when(mockedDirectoryWithCommandsFile.hasFile(CASENAME1 + ".output")).thenReturn(true);
		when(mockedDirectoryWithCommandsFile.getFile(CASENAME1 + ".output")).thenReturn(mockedFile);

		BufferedReader mockedBufferReader = mock(BufferedReader.class);
		when(mockedDirectoryWithCommandsFile.getBufferedReaderForFile((File) any())).thenReturn(mockedBufferReader);
		when(mockedBufferReader.readLine()).thenReturn(EXTENSION + " : command");

		regrDirectoryWithoutCommandsFile = new RegrDirectory(mockedDirectoryWithoutCommandsFile);
		regrDirectoryWithCommandsFile = new RegrDirectory(mockedDirectoryWithCommandsFile);
	}

	@Test
	public void returnsNoCasesForDirectoryWithNoFilesMatchingExtension() throws Exception {
		when(mockedDirectoryWithoutCommandsFile.getFilenamesWithExtension(EXTENSION)).thenReturn(NO_FILENAMES);

		assertTrue(Arrays.equals(NO_CASES, regrDirectoryWithCommandsFile.getCases()));
	}

	@Test
	public void returnsOneCaseForDirectoryWithSingleFileMatchingExtension() throws Exception {
		when(mockedDirectoryWithCommandsFile.getFilenamesWithExtension(EXTENSION)).thenReturn(ONE_FILENAME_MATCHING_EXTENSION);

		final RegrCase[] cases = regrDirectoryWithCommandsFile.getCases();
		assertEquals(1, cases.length);
		assertEquals(CASENAME1, cases[0].getName());
	}

	@Test
	public void returnsTwoCasesForDirectoryWithTwoFilesMatchingExtension() throws Exception {
		when(mockedDirectoryWithCommandsFile.getFilenamesWithExtension(EXTENSION)).thenReturn(TWO_FILENAMES_MATCHING_EXTENSION);

		final RegrCase[] cases = regrDirectoryWithCommandsFile.getCases();
		assertEquals(2, cases.length);
		assertEquals(CASENAME1, cases[0].getName());
		assertEquals(CASENAME2, cases[1].getName());
	}

	@Test
	public void canReturnJRegrFile() throws Exception {
		assertEquals(jregrFile, regrDirectoryWithCommandsFile.getCommandsFile());
	}

	@Test
	public void testRegrDirectoryReturnsNullIfNoJRegrFile() throws Exception {
		assertEquals(null, regrDirectoryWithoutCommandsFile.getCommandsFile());
	}

	@Test
	public void testRegrDirectoryShouldReturnOutputFile() throws Exception {
		assertEquals(mockedFile, regrDirectoryWithCommandsFile.getOutputFile(CASENAME1));
	}

	@Test
	public void testCanSeeIfCaseFileExists() throws Exception {
		when(mockedDirectoryWithCommandsFile.hasFile(FILENAME1)).thenReturn(true);

		assertTrue(regrDirectoryWithCommandsFile.hasCaseFile(CASENAME1));
	}

	
}