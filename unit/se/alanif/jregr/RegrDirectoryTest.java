package se.alanif.jregr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

public class RegrDirectoryTest {

	private static final String EXTENSION = ".extension";

	private static final String FILENAME1 = "file1";
	private static final String FILENAME2 = "file2";

	private static final String CASENAME1 = FILENAME1;

	private static final String CASEFILENAME_WITH_EXTENSION_1 = CASENAME1 + EXTENSION;

	private static final String[] NO_FILENAMES = new String[] {};
	private static final String[] ONE_FILENAME_WITH_EXTENSION = new String[] { CASEFILENAME_WITH_EXTENSION_1 };
	private static final String[] TWO_FILENAMES_NONE_MATCHING_EXTENSION = new String[] { FILENAME1, FILENAME2 };

	private final Directory mockedDirectoryWithoutCommandsFile = mock(Directory.class);
	private final Directory mockedDirectoryWithCommandsFile = mock(Directory.class);

	private final Runtime mockedRuntime = mock(Runtime.class);
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

		regrDirectoryWithoutCommandsFile = new RegrDirectory(mockedDirectoryWithoutCommandsFile, mockedRuntime);
		regrDirectoryWithCommandsFile = new RegrDirectory(mockedDirectoryWithCommandsFile, mockedRuntime);
	}

	// Implements the filtering that a real directory would do
	private class TestMatcher implements Answer<String[]> {
		private String[] filenames;

		public TestMatcher(String[] filenames) {
			this.filenames = filenames;
		}

		public String[] answer(InvocationOnMock invocation) {
			FilenameFilter filter = (FilenameFilter) invocation.getArguments()[0];
			ArrayList<String> matchedFilenames = new ArrayList<String>();
			for (String name : filenames)
				if (filter.accept(null, name))
					matchedFilenames.add(name);
			String[] stringArray = new String[matchedFilenames.size()];
			return matchedFilenames.toArray(stringArray);
		}
	}

	@Test
	public void testDirectoryReturnsNoCasesForEmptyDirectoryWithoutCommandsFile() throws Exception {
		when(mockedDirectoryWithoutCommandsFile.list((FilenameFilter) any())).thenAnswer(new TestMatcher(NO_FILENAMES));

		assertTrue(Arrays.equals(NO_CASES, regrDirectoryWithoutCommandsFile.getCases()));
	}

	@Test
	public void testDirectoryReturnsNoCasesForEmptyDirectoryWithCommandsFile() throws Exception {
		when(mockedDirectoryWithoutCommandsFile.list((FilenameFilter) any())).thenAnswer(new TestMatcher(NO_FILENAMES));

		assertTrue(Arrays.equals(NO_CASES, regrDirectoryWithCommandsFile.getCases()));
	}

	@Test
	public void testDirectoryReturnsNoCasesForDirectoryWithNoFileMatchingExplicitExtension() throws Exception {
		when(mockedDirectoryWithCommandsFile.list((FilenameFilter) any()))
				.thenAnswer(new TestMatcher(TWO_FILENAMES_NONE_MATCHING_EXTENSION));

		assertTrue(Arrays.equals(NO_CASES, regrDirectoryWithCommandsFile.getCases()));
	}

	@Test
	public void testDirectoryReturnsOneCaseForDirectoryWithSingleFileMatchingExtension() throws Exception {
		when(mockedDirectoryWithCommandsFile.list((FilenameFilter) any()))
				.thenAnswer(new TestMatcher(ONE_FILENAME_WITH_EXTENSION));

		final RegrCase[] cases = regrDirectoryWithCommandsFile.getCases();
		assertEquals(1, cases.length);
		assertEquals(CASENAME1, cases[0].getName());
	}

	@Test
	public void testRegrDirectoryReturnsJRegrFile() throws Exception {
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
		when(mockedDirectoryWithCommandsFile.hasFile(CASEFILENAME_WITH_EXTENSION_1)).thenReturn(true);

		assertTrue(regrDirectoryWithCommandsFile.hasCaseFile(CASENAME1));
	}

}
