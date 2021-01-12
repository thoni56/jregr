package se.alanif.jregr.exec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

import junit.framework.TestCase;
import se.alanif.jregr.CommandsDecoder;
import se.alanif.jregr.RegrDirectory;
import se.alanif.jregr.exec.RegrCase.State;
import se.alanif.jregr.io.Directory;
import se.alanif.jregr.io.File;
import se.alanif.jregr.reporters.RegrReporter;

public class RegrRunnerTest extends TestCase {

	private static final String CASENAME = "test1";
	private static final String SUITENAME = "suite.name";

	private static final RegrCase mockedCase = mock(RegrCase.class);
	private static final RegrCase[] NO_CASES = new RegrCase[] {};
	private static final RegrCase[] ONE_CASE = new RegrCase[] { mockedCase };

	private RegrReporter mockedReporter = mock(RegrReporter.class);

	private Directory binDirectory = mock(Directory.class);
	private File mockedOutputFile = mock(File.class);
	private CommandsDecoder mockedDecoder = mock(CommandsDecoder.class);


	// Private sub-class of the SUT with a few overridden utility functions
	// So that we can return what we need
	private RegrCase[] casesToReturn;
	private class MockedRegrDirectory extends RegrDirectory {

		public MockedRegrDirectory(Directory directory, Runtime runtime) throws IOException {
			super(directory, runtime);
		}

		@Override
		public RegrCase[] getCases() {
			return casesToReturn;
		}
	}
	private MockedRegrDirectory regrDirectory;

	protected void setUp() throws Exception {
		regrDirectory = new MockedRegrDirectory(binDirectory, null);
		regrDirectory.setDecoder(mockedDecoder);
		
		when(mockedOutputFile.getPath()).thenReturn("outputFile");
		when(mockedCase.getName()).thenReturn(CASENAME);
		when(mockedCase.getOutputFile()).thenReturn(mockedOutputFile);
	}

	@Test
	public void testRunnerOnNoCasesShouldNotReportAnyTests() throws Exception {
		assertTrue(regrDirectory.runSelectedCases(NO_CASES, mockedReporter, null, SUITENAME, null, null));
		verify(mockedReporter, never()).starting(mockedCase, 0);
	}

	@Test
	public void testRunnerInDirectoryWithOneTestShouldReport() throws Exception {
		assertTrue(regrDirectory.runSelectedCases(ONE_CASE, mockedReporter, null, SUITENAME, mockedDecoder, null));

		verify(mockedReporter).starting(eq(mockedCase), longThat(millis -> millis == 0));
	}

	@Test
	public void testRunCasesInADirectoryWithASingleCaseShouldRunOneCaseAndReport() throws Exception {
		casesToReturn = ONE_CASE;

		regrDirectory.runAllCases(mockedReporter, binDirectory, SUITENAME, mockedDecoder, null);

		verify(mockedCase).run(eq(binDirectory), (CommandsDecoder) any(), (PrintWriter) any(), (CaseRunner) any(),
				(ProcessBuilder) any());
		verify(mockedReporter).starting(eq(mockedCase), longThat(millis -> millis == 0));
		verify(mockedReporter).report((State) any());
	}

}
