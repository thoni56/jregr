package se.alanif.jregr;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;
import se.alanif.jregr.reporters.RegrReporter;

public class RegrDirectoryRecursionTest {

	private static final String CURRENT_DIRECTORY = "somePath";

	private static final String EXTENSION = ".extension";

	private static final String FILENAME1 = "file1";

	private static final String CASENAME1 = FILENAME1;

	private static final Directory mockedCurrentDirectory = mock(Directory.class);
	private static final Directory mockedSubDirectoryWithoutJregr = mock(Directory.class);
	private static final Directory mockedSubDirectoryWithJregr = mock(Directory.class);
	
	private static final Directory mockedBinDirectory = mock(Directory.class);

	private static final Directory[] NO_SUBDIRECTORIES = new Directory[] {};
	private static final Directory[] ONE_SUBDIRECTORY_WITHOUT_JREGR = new Directory[] {mockedSubDirectoryWithoutJregr};
	private static final Directory[] ONE_SUBDIRECTORY_WITH_JREGR = new Directory[] {mockedSubDirectoryWithJregr};
	
	private final Runtime mockedRuntime = mock(Runtime.class);
	private final File mockedFile = new File("mockedFile");
	
	private static final RegrReporter mockedReporter = mock(RegrReporter.class);

	private File jregrFile = new File(RegrDirectory.COMMANDS_FILE_NAME);
	
	private RegrCase mockedPassingCase = mock(RegrCase.class);
	private RegrCase mockedFailingCase = mock(RegrCase.class);

	// A double of the class under test, but we need to control the cases it returns
	private RegrCase[] returnedCases = null;
	private class RegrDirectoryDouble extends RegrDirectory {

		public RegrDirectoryDouble(Directory directory, Runtime runtime) throws IOException {
			super(directory, runtime);
		}
		
		@Override
		public RegrCase[] getCases() {
			return returnedCases;
		}
		
	}

	private RegrDirectoryDouble currentRegrDirectory;

	private static final CommandLine mockedCommandLine = mock(CommandLine.class);
	private static final CommandsDecoder mockedDecoder = mock(CommandsDecoder.class);
	
	@Before
	public void setUp() throws Exception {
		when(mockedSubDirectoryWithoutJregr.hasFile(RegrDirectory.COMMANDS_FILE_NAME)).thenReturn(false);
		
		when(mockedSubDirectoryWithJregr.hasFile(RegrDirectory.COMMANDS_FILE_NAME)).thenReturn(true);
		when(mockedSubDirectoryWithJregr.getFile(RegrDirectory.COMMANDS_FILE_NAME)).thenReturn(jregrFile);
		when(mockedSubDirectoryWithJregr.hasFile(CASENAME1 + ".output")).thenReturn(true);
		when(mockedSubDirectoryWithJregr.getFile(CASENAME1 + ".output")).thenReturn(mockedFile);
		
		when(mockedCurrentDirectory.getAbsolutePath()).thenReturn(CURRENT_DIRECTORY);

		currentRegrDirectory = new RegrDirectoryDouble(mockedCurrentDirectory, mockedRuntime);

		when(mockedPassingCase.failed()).thenReturn(false);
		when(mockedFailingCase.failed()).thenReturn(true);

		BufferedReader mockedBufferReader = mock(BufferedReader.class);
		when(mockedCurrentDirectory.getBufferedReaderForFile((File) any())).thenReturn(mockedBufferReader);
		when(mockedBufferReader.readLine()).thenReturn(EXTENSION + " : command");
	}

	@Test
	public void shouldReturnTrueIfNoSubdirectories() throws Exception {
		when(mockedCurrentDirectory.getSubdirectories()).thenReturn(NO_SUBDIRECTORIES);
		assertTrue(currentRegrDirectory.recurse(mockedReporter, mockedBinDirectory, null, null, null));
	}
	
	@Test
	public void shouldReturnTrueIfNoSubdirectoriesWithJregr() throws Exception {
		when(mockedCurrentDirectory.getSubdirectories()).thenReturn(ONE_SUBDIRECTORY_WITHOUT_JREGR);
		assertTrue(currentRegrDirectory.recurse(mockedReporter, mockedBinDirectory, null, null, null));
	}
	
	@Test
	public void shouldReturnTrueIfSingleSubdirectoryWithJregrButNoCases() throws Exception {
		when(mockedCurrentDirectory.getSubdirectories()).thenReturn(ONE_SUBDIRECTORY_WITH_JREGR);
		returnedCases = new RegrCase[0];
		assertTrue(currentRegrDirectory.recurse(mockedReporter, mockedBinDirectory, null, null, null));
	}
	
	@Test
	public void shouldReturnResultOfCaseInSingleSubdirectoryWithJregr() throws Exception {
		when(mockedCurrentDirectory.getSubdirectories()).thenReturn(ONE_SUBDIRECTORY_WITH_JREGR);
		returnedCases = new RegrCase[]{ mockedPassingCase };
		assertTrue(currentRegrDirectory.recurse(mockedReporter, mockedBinDirectory, "", mockedDecoder , mockedCommandLine ));
	}

}
