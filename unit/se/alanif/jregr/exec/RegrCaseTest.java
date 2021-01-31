package se.alanif.jregr.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import se.alanif.jregr.CommandDecoder;
import se.alanif.jregr.RegrDirectory;
import se.alanif.jregr.exec.RegrCase.State;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;

public class RegrCaseTest {

	private static final String CASENAME = "theCase";

	private static final String BIN_DIRECTORY_PATH = "directory";
	private static final String BIN_DIRECTORY_PATH_WITH_SEPARATOR = BIN_DIRECTORY_PATH + Directory.separator;

	private static final String COMMAND1 = "alan";
	private static final String COMMAND1_PREPENDED_WITH_BIN_DIRECTORY = BIN_DIRECTORY_PATH_WITH_SEPARATOR + COMMAND1;
	private static final String ARGUMENT1 = "arg11";

	private static final String[] COMMAND1_AND_ARGUMENTS = new String[] { COMMAND1_PREPENDED_WITH_BIN_DIRECTORY,
			ARGUMENT1 };
	private static final String[] COMMAND1_AND_CASENAME = new String[] { COMMAND1_PREPENDED_WITH_BIN_DIRECTORY,
			CASENAME };

	private static final String COMMAND2 = "command2";
	private static final String COMMAND2_PREPENDED_WITH_BIN_DIRECTORY = BIN_DIRECTORY_PATH_WITH_SEPARATOR + COMMAND2;
	private static final String ARGUMENT2_1 = "arg1";
	private static final String ARGUMENT2_2 = "-opt";
	private static final String[] COMMAND2_AND_ARGUMENTS = { COMMAND2_PREPENDED_WITH_BIN_DIRECTORY, ARGUMENT2_1,
			ARGUMENT2_2 };

	private CommandDecoder mockedDecoder = mock(CommandDecoder.class);
	private Directory binDirectory = mock(Directory.class);

	private Directory mockedDirectory = mock(Directory.class);
	private RegrDirectory mockedRegrDirectory = mock(RegrDirectory.class);
	private CommandRunner mockedCommandRunner = mock(CommandRunner.class);
	private PrintWriter mockedPrinter = mock(PrintWriter.class);

	private Process mockedProcess = mock(Process.class);

	private RegrCase theCase = new RegrCase(CASENAME, mockedRegrDirectory);

	private InputStream mockedInputStream = mock(InputStream.class);

	private OutputStream mockedOutputStream = mock(OutputStream.class);

	private File mockedExpectedFile = mock(File.class);

	@Before
	public void setUp() throws Exception {
		when(binDirectory.getAbsolutePath()).thenReturn(BIN_DIRECTORY_PATH);
		when(mockedProcess.getErrorStream()).thenReturn(mockedInputStream);
		when(mockedProcess.getInputStream()).thenReturn(mockedInputStream);
		when(mockedProcess.getOutputStream()).thenReturn(mockedOutputStream);
		when(mockedRegrDirectory.getExpectedFile(CASENAME)).thenReturn(mockedExpectedFile);
		when(mockedRegrDirectory.toDirectory()).thenReturn(mockedDirectory);
		when(mockedRegrDirectory.exists(any())).thenReturn(true);
	}

	@Test
	public void shouldExecTheCommandAndArgumentsFromTheDecoder() throws Exception {
		when(mockedDecoder.buildCommandAndArguments(binDirectory, CASENAME)).thenReturn(COMMAND1_AND_CASENAME);

		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner);

		verify(mockedCommandRunner).runCommandForOutput(eq(COMMAND1_AND_CASENAME), any(), any());
	}

	@Test
	public void shouldRunByExecutingEveryCommand() throws Exception {
		when(mockedDecoder.buildCommandAndArguments(binDirectory, CASENAME)).thenReturn(COMMAND1_AND_ARGUMENTS)
				.thenReturn(COMMAND2_AND_ARGUMENTS);
		when(mockedDecoder.advance()).thenReturn(true).thenReturn(false);

		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner);

		verify(mockedCommandRunner).runCommandForOutput(eq(COMMAND1_AND_ARGUMENTS), any(), any());
		verify(mockedCommandRunner).runCommandForOutput(eq(COMMAND2_AND_ARGUMENTS), any(), any());
	}

	@Test
	public void shouldReturnPassIfExpectedButNoOutputFileExists() throws Exception {
		when(mockedRegrDirectory.hasExpectedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasOutputFile(theCase.getName())).thenReturn(false);

		assertEquals(State.PASS, theCase.status());
	}

	@Test
	public void shouldReturnPendingIfNoExpectedButOutputFileExists() throws Exception {
		when(mockedRegrDirectory.hasExpectedFile(theCase.getName())).thenReturn(false);
		when(mockedRegrDirectory.hasOutputFile(theCase.getName())).thenReturn(true);

		assertEquals(State.PENDING, theCase.status());
	}

	@Test
	public void shouldReturnFailIfExpectedAndOutputFileExists() throws Exception {
		when(mockedRegrDirectory.hasSuspendedFile(theCase.getName())).thenReturn(false);
		when(mockedRegrDirectory.hasExpectedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasOutputFile(theCase.getName())).thenReturn(true);

		assertEquals(State.FAIL, theCase.status());
	}

	@Test
	public void shouldReturnSuspendedFailIfSuspendedAndOutputFileExists() throws Exception {
		when(mockedRegrDirectory.hasExpectedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasSuspendedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasOutputFile(theCase.getName())).thenReturn(true);

		assertEquals(State.SUSPENDED_FAIL, theCase.status());
	}

	@Test
	public void shouldReturnSuspendedPassIfSuspendedAndOutputFileDoesntExists() throws Exception {
		when(mockedRegrDirectory.hasSuspendedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasExpectedFile(theCase.getName())).thenReturn(true);
		when(mockedRegrDirectory.hasOutputFile(theCase.getName())).thenReturn(false);

		assertEquals(State.SUSPENDED_PASS, theCase.status());
	}

	@Test
	public void shouldWriteToOutputFileAndCloseIt() throws Exception {
		when(mockedDecoder.buildCommandAndArguments(binDirectory, CASENAME)).thenReturn(COMMAND1_AND_ARGUMENTS);
		PrintWriter mockedWriter = mock(PrintWriter.class);
		when(mockedCommandRunner.runCommandForOutput(any(), any(), any())).thenReturn("the output");

		theCase.run(binDirectory, mockedDecoder, mockedWriter, mockedCommandRunner);

		verify(mockedWriter, atLeastOnce()).print("the output");
		verify(mockedWriter).close();
	}

	@Test
	public void canSeeIfACaseExists() throws Exception {
		when(mockedRegrDirectory.hasCaseFile(CASENAME)).thenReturn(true);
		assertTrue(theCase.exists());
	}

	@Test
	public void canGetOutputFile() throws Exception {
		File mockedOutputFile = mock(File.class);
		when(mockedRegrDirectory.getOutputFile(CASENAME)).thenReturn(mockedOutputFile);
		assertEquals(mockedOutputFile, theCase.getOutputFile());
	}
	
	@Test
	public void willNotExecuteIfFileWithExtensionDoesNotExist() throws Exception {
		when(mockedDecoder.getExtension()).thenReturn(".ext");
		when(mockedRegrDirectory.exists(CASENAME+".ext")).thenReturn(false);
		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner);
		verify(mockedCommandRunner, never()).runCommandForOutput(any(), any(), any());
	}

	@Test
	public void willNotPrintMessageForFileThatDoesNotExistIfOptional() throws Exception {
		when(mockedDecoder.getExtension()).thenReturn(".ext");
		when(mockedRegrDirectory.exists(CASENAME+".ext")).thenReturn(false);
		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner);
		verify(mockedPrinter, never()).println(anyString());
	}

}
