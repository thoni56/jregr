package se.alanif.jregr.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import se.alanif.jregr.CommandsDecoder;
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

	private CommandsDecoder mockedDecoder = mock(CommandsDecoder.class);
	private Runtime mockedRuntime = mock(Runtime.class);
	private Directory binDirectory = mock(Directory.class);

	private Directory mockedDirectory = mock(Directory.class);
	private RegrDirectory mockedRegrDirectory = mock(RegrDirectory.class);
	private CommandRunner mockedCommandRunner = mock(CommandRunner.class);
	private PrintWriter mockedPrinter = mock(PrintWriter.class);

	private Process mockedProcess = mock(Process.class);
	private ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

	private RegrCase theCase = new RegrCase(mockedRuntime, CASENAME, mockedRegrDirectory);

	private InputStream mockedInputStream = mock(InputStream.class);

	private OutputStream mockedOutputStream = mock(OutputStream.class);

	private File mockedExpectedFile = mock(File.class);

	@Before
	public void setUp() throws Exception {
		when(binDirectory.getAbsolutePath()).thenReturn(BIN_DIRECTORY_PATH);
		when(mockedRuntime.exec((String[]) any())).thenReturn(mockedProcess);
		when(mockedProcessBuilder.exec((Directory) any(), eq(mockedRuntime), (String[]) any())).thenReturn(mockedProcess);
		when(mockedProcess.getErrorStream()).thenReturn(mockedInputStream);
		when(mockedProcess.getInputStream()).thenReturn(mockedInputStream);
		when(mockedProcess.getOutputStream()).thenReturn(mockedOutputStream);
		when(mockedRegrDirectory.getExpectedFile(CASENAME)).thenReturn(mockedExpectedFile);
		when(mockedRegrDirectory.toDirectory()).thenReturn(mockedDirectory);
	}

	@Test
	public void shouldExecTheCommandAndArgumentsFromTheDecoder() throws Exception {
		when(mockedDecoder.buildCommandAndArguments(binDirectory, CASENAME)).thenReturn(COMMAND1_AND_CASENAME);

		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner, mockedProcessBuilder);

		verify(mockedProcessBuilder).exec(mockedDirectory, mockedRuntime, COMMAND1_AND_CASENAME);
	}

	@Test
	public void shouldRunByExecutingEveryCommand() throws Exception {
		when(mockedDecoder.buildCommandAndArguments(binDirectory, CASENAME)).thenReturn(COMMAND1_AND_ARGUMENTS)
				.thenReturn(COMMAND2_AND_ARGUMENTS);
		when(mockedDecoder.advance()).thenReturn(true).thenReturn(false);

		theCase.run(binDirectory, mockedDecoder, mockedPrinter, mockedCommandRunner, mockedProcessBuilder);

		verify(mockedProcessBuilder).exec(mockedDirectory, mockedRuntime, COMMAND1_AND_ARGUMENTS);
		verify(mockedProcessBuilder).exec(mockedDirectory, mockedRuntime, COMMAND2_AND_ARGUMENTS);
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
		PrintWriter mockedWriter = mock(PrintWriter.class);
		when(mockedCommandRunner.run((Process) any(), (StreamGobbler) any(), (StreamGobbler) any(), (StreamPusher) any()))
				.thenReturn("the output");

		theCase.run(binDirectory, mockedDecoder, mockedWriter, mockedCommandRunner, mockedProcessBuilder);

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

}
