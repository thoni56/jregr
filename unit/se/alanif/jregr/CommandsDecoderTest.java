package se.alanif.jregr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import se.alanif.jregr.io.Directory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandsDecoderTest {
	
	private static final String BINPATH = "binpath";
	private static final String CASENAME = "case";

	private static final String EXTENSION1 = ".extension1";
	private static final String COMMAND1 = "command1";
	private static final String ARG11 = "arg11";
	private static final String ARG12 = "arg12";
	private static final String[] FIRST_COMMAND_AND_ARGUMENTS = new String[] {BINPATH+File.separator+COMMAND1, ARG11, ARG12};
	private static final String[] COMMAND_AND_ARGUMENTS_WITHOUT_PATH = new String[] {COMMAND1, ARG11, ARG12};

	private static final String EXTENSION2 = ".extension2";
	private static final String COMMAND2 = "command2";
	private static final String ARG21 = "arg11";
	private static final String OPT21 = "-option";
	private static final String OPTION_VALUE_21 = "12";
	private static final String STDIN = "stdin";
	private static final String[] SECOND_COMMAND_AND_ARGUMENTS = new String[] {BINPATH+File.separator+COMMAND2, ARG21, OPT21, OPTION_VALUE_21};
	
	private BufferedReader mockedFileReader = mock(BufferedReader.class);
	private Directory binDirectory = mock(Directory.class);
	private Directory binDirectoryWithoutExecutables = mock(Directory.class);
	private CommandsDecoder decoder;

	@Before
	public void setUp() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(EXTENSION1+" : "+COMMAND1+" "+ARG11+" "+ARG12);
		
		File mockedFile = mock(File.class);
		when(mockedFile.exists()).thenReturn(true);
		
		when(binDirectory.getAbsolutePath()).thenReturn(BINPATH);
		when(binDirectory.executableExist(anyString())).thenReturn(true);
		when(binDirectoryWithoutExecutables.executableExist(anyString())).thenReturn(false);
		
		decoder = new CommandsDecoder(mockedFileReader);
		decoder.reset(CASENAME);
	}
	
	@Test
	public void canGetExtensionFromCurrentJregrLine() throws Exception {
		assertEquals(EXTENSION1, decoder.getExtension());
	}

	@Test
	public void shouldBuildCommandAndArgumentsFromCurrentJregrLine() throws Exception {
		assertTrue(Arrays.equals(FIRST_COMMAND_AND_ARGUMENTS, decoder.buildCommandAndArguments(binDirectory, CASENAME)));
	}
	
	@Test
	public void shouldBuildWithoutBinDirPathIfExecutableDoesNotExist() throws Exception {
		assertTrue(Arrays.equals(COMMAND_AND_ARGUMENTS_WITHOUT_PATH, decoder.buildCommandAndArguments(binDirectoryWithoutExecutables, CASENAME)));
	}
	
	@Test
	public void shouldGetStdinFileFromCurrentJregrLine() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(EXTENSION1+" : "+COMMAND1+" "+ARG11+" "+ARG12 + " < " + STDIN);
		decoder = new CommandsDecoder(mockedFileReader);
		
		assertEquals(STDIN, decoder.getStdin());
	}

	@Test
	public void returnsFalseWhenAdvancingBeyondEndOfCommands() throws Exception {
		when(mockedFileReader.readLine())
			.thenReturn(EXTENSION2+" : "+COMMAND2+" "+ARG21+" "+OPT21+" "+OPTION_VALUE_21)
			.thenThrow(new IOException());

		decoder.advance();
		assertFalse(decoder.advance());
	}

	@Test
	public void shouldNotAdvanceAfterEndOfJregrFile() throws Exception {
		when(mockedFileReader.readLine())
			.thenReturn("extension : command")
			.thenThrow(new IOException());
		assertTrue(decoder.advance());
		assertFalse(decoder.advance());
		assertFalse(decoder.advance());
	}

	@Test
	public void shouldBuildCommandAndArgumentsAfterAdvancingToSecondLine() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(EXTENSION2+" : "+COMMAND2+" "+ARG21+" "+OPT21+" "+OPTION_VALUE_21);
		
		decoder.advance();
		assertTrue(Arrays.equals(SECOND_COMMAND_AND_ARGUMENTS, decoder.buildCommandAndArguments(binDirectory, CASENAME)));
	}
	
	@Test
	public void canResetDecoderToReadFromBeginning() throws Exception {
		decoder.advance();
		decoder.reset();
		
		verify(mockedFileReader).mark(anyInt());
		verify(mockedFileReader, atLeast(1)).reset();
		assertTrue(Arrays.equals(FIRST_COMMAND_AND_ARGUMENTS, decoder.buildCommandAndArguments(binDirectory, CASENAME)));
	}
	
	@Test
	public void canFindStdinWhenLast() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(".ext : command with arguments < input_file");
		decoder.advance();
		assertEquals("input_file", decoder.getStdin());
	}
	
	@Test
	public void canFindStdoutWhenLast() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(".ext : command with arguments > output_file");
		decoder.advance();
		assertEquals("output_file", decoder.getStdout());
	}

	@Test
	public void canFindBothStdinAndStdout() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(".ext : command with arguments < input_file > output_file");
		decoder.advance();
		assertEquals("input_file", decoder.getStdin());
		assertEquals("output_file", decoder.getStdout());
	}

	@Test
	public void canFindBothStdoutAndStdin() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(".ext : command with arguments > output_file < input_file");
		decoder.advance();
		assertEquals("input_file", decoder.getStdin());
		assertEquals("output_file", decoder.getStdout());
	}

	@Test
	public void canExandSymbolsStdin() throws Exception {
		when(mockedFileReader.readLine()).thenReturn(".ext : command with arguments < $1.input");
		decoder.advance();
		assertEquals(CASENAME+".input", decoder.getStdin());
	}
}
