package se.alanif.jregr.exec;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.junit.Test;


public class CommandRunnerTest {
	
	StreamGobbler mockedErrorGobbler = mock(StreamGobbler.class);
	StreamGobbler mockedOutputGobbler = mock(StreamGobbler.class);
	StreamPusher mockedInputPusher = mock(StreamPusher.class);
	Process p = mock(Process.class);

	CommandRunner commandRunner = new CommandRunner();
	private ProcessBuilder mockedProcessBuilder;

	@Test
	public void shouldReturnEmptyOutputIfGobblersReturnNothing() throws Exception {
		when(mockedErrorGobbler.output()).thenReturn("");
		when(mockedOutputGobbler.output()).thenReturn("");

		assertEquals("", commandRunner.run(p, mockedErrorGobbler, mockedOutputGobbler, mockedInputPusher));
	}
	
	@Test
	public void shouldReturnResultFromErrorAndOutput() throws Exception {
		when(mockedErrorGobbler.output()).thenReturn("error");
		when(mockedOutputGobbler.output()).thenReturn("output");

		assertTrue(commandRunner.run(p, mockedErrorGobbler, mockedOutputGobbler, mockedInputPusher).contains("error"));
		assertTrue(commandRunner.run(p, mockedErrorGobbler, mockedOutputGobbler, mockedInputPusher).contains("output"));
	}
	
	@Test
	public void canRunACommandAndReturnOutputAsArrayOfStrings() {
		String[] output = commandRunner.runCommandForOutput(new String[]{"command"}, mockedProcessBuilder);
		verify(mockedProcessBuilder).command("command");
	}
}
