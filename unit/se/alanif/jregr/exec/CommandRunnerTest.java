package se.alanif.jregr.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


public class CommandRunnerTest {
	
	private static final String OUTPUT = "output";
	private static final String ERROR = "error";
	
	private StreamGobbler mockedErrorGobbler = mock(StreamGobbler.class);
	private StreamGobbler mockedOutputGobbler = mock(StreamGobbler.class);
	private StreamPusher mockedInputPusher = mock(StreamPusher.class);
	private Process mockedProcess = mock(Process.class);

	private CommandRunner commandRunner = new CommandRunner();
	private ProcessBuilderSpy mockedProcessBuilder = mock(ProcessBuilderSpy.class);
	
	@Before
	public void setUp() throws IOException {
		commandRunner.setProcessBuilder(mockedProcessBuilder);
		commandRunner.setGobblers(mockedOutputGobbler, mockedErrorGobbler);
		commandRunner.setInputPusher(mockedInputPusher);

		when(mockedProcessBuilder.start()).thenReturn(mockedProcess);

		when(mockedErrorGobbler.output()).thenReturn(ERROR);
		when(mockedOutputGobbler.output()).thenReturn(OUTPUT);
	}

	@Test
	public void shouldReturnResultFromOutputAndError() throws Exception {
		String output = commandRunner.runCommandForOutput(new String[]{"command"}, null, null);
		assertTrue(output.contains(ERROR));
		assertTrue(output.contains(OUTPUT));
	}
	
	@Test
	public void canRunACommandForOutput() throws Exception {
		String output = commandRunner.runCommandForOutput(new String[]{"command"}, null, null);
		assertEquals(OUTPUT+ERROR, output);
		verify(mockedProcessBuilder).command(new String[]{"command"});
	}
	
	@Test
	public void willStartInputPusherForInput() throws Exception {
		commandRunner.runCommandForOutput(new String[]{"command"}, "inputFile", null);
		verify(mockedInputPusher).run();
	}
	
	
}
