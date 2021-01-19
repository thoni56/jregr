package se.alanif.jregr.reporters;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.exec.RegrCase;


public class ConsoleReporter extends AbstractRegrReporter {

	private static final String CSI = "\33[";

	private static final String TEXT_COLOR_RED = ""+31;
	private static final String TEXT_COLOR_GREEN = ""+32;
	private static final String TEXT_COLOR_YELLOW = ""+33;
	private static final String TEXT_COLOR_BLUE = ""+34;
	private static final String TEXT_COLOR_MAGENTA = ""+35;
	private static final String TEXT_COLOR_CYAN = ""+36;
	private static final String TEXT_COLOR_DEFAULT = ""+0;

	private static final String VIRGIN = TEXT_COLOR_CYAN;
	private static final String PENDING = TEXT_COLOR_BLUE;
	private static final String SUSPENDED = TEXT_COLOR_YELLOW;
	private static final String PASSED = TEXT_COLOR_GREEN;
	private static final String FAILED = TEXT_COLOR_RED;
	private static final String FATAL = TEXT_COLOR_MAGENTA;
	private static final String DEFAULT = TEXT_COLOR_DEFAULT;

	private String possibleComma = "";
	private CommandLine commandLine;

	private int total_tests;
	private int total_fatal;
	private int total_failing;
	private int total_suspended;
	private int total_pending;
	private int total_passing;

	private String suite;

	public void start(CommandLine commandLine) {
		total_tests = 0;
		total_fatal = 0;
		total_failing = 0;
		total_suspended = 0;
		total_pending = 0;
		total_passing = 0;

		this.commandLine = commandLine;
	}

	public void startSuite(String suite, int numberOfTests) {
		this.suite = suite;
		tests = numberOfTests;
		fatal = 0;
		failing = 0;
		suspended = 0;
		pending = 0;
		passing = 0;
		System.out.println("Running " + numberOfTests + " test(s) in '" + suite + "' :");
	}

	public void startTest(RegrCase theCase, long millis) {
		System.out.print(theCase.getName() + " : ");
	}

	public void fatal() {
		fatal++;
		color(FATAL);
		System.out.println("Fatal");
		color(DEFAULT);
	}

	public void virgin() {
		virgin++;
		color(VIRGIN);
		System.out.println("Virgin");
		color(DEFAULT);
	}

	public void pending() {
		pending++;
		color(PENDING);
		System.out.println("Pending");
		color(DEFAULT);
	}

	public void fail() {
		failing++;
		color(FAILED);
		System.out.println("Fail");
		color(DEFAULT);
	}

	public void pass() {
		passing++;
		color(PASSED);
		System.out.print("Pass");
		color(DEFAULT);
		if (commandLine.hasOption("noansi") || commandLine.hasOption("verbose")) {
			System.out.println();
		} else {
			eraseRestOfLine();
			System.out.print("\r");
		}
	}

	private void eraseRestOfLine() {
		String eraseLine = CSI+"1K";
		System.out.print(eraseLine);
	}

	public void suspended() {
		suspended++;
		color(SUSPENDED);
		System.out.println("Suspended");
		color(DEFAULT);
	}

	public void suspendedAndFailed() {
		suspended++;
		color(SUSPENDED);
		System.out.print("Suspended (and ");
		color(FAILED);
		System.out.print("failed");
		color(SUSPENDED);
		System.out.println(")");
		color(DEFAULT);
	}

	public void suspendedAndPassed() {
		suspended++;
		color(SUSPENDED);
		System.out.print("Suspended (and ");
		color(PASSED);
		System.out.print("passed");
		color(SUSPENDED);
		System.out.println(")");
		color(DEFAULT);
	}

	public void endSuite() {
		possibleComma = "";
		if (fatal > 0) color(FATAL);
		else if (failing > 0) color(FAILED);
		else color(PASSED);
		System.out.printf("%s: %d test(s)", suite, tests);
		color(DEFAULT);
		if (fatal > 0 || suspended > 0 || pending > 0 || failing > 0 || passing > 0) {
			System.out.print(" (");
			if (fatal > 0) record(fatal, "fatal", FATAL);
			if (suspended > 0) recordSuspended();
			if (pending > 0) recordPending();
			if (failing > 0) recordFailing();
			if (passing > 0) recordPassing();
			color(DEFAULT);
			System.out.printf(")");
		}
		System.out.printf("\n");
		
		total_fatal += fatal;
		total_failing += failing;
		total_suspended += suspended;
		total_pending += pending;
		total_passing += passing;
		total_tests += tests;		
	}

	private void recordPassing() {
		record(passing, "passing", PASSED);
	}

	private void recordFailing() {
		record(failing, "failing", FAILED);
	}

	private void recordPending() {
		record(pending, "pending", PENDING);
	}

	private void recordSuspended() {
		record(suspended, "suspended", SUSPENDED);
	}

	private void color(String color) {
		if (!commandLine.hasOption("nocolor") && !commandLine.hasOption("nocolour"))
			System.out.print(CSI+color+"m");
	}

	private void record(int count, String type, String typeColor) {
		System.out.print(possibleComma);
		color(typeColor);
		System.out.printf("%d %s", count, type);
		possibleComma = ", ";
	}
	
	public void end() {
		possibleComma = "";
		if (total_fatal > 0) color(FATAL);
		else if (total_failing > 0) color(FAILED);
		else color(PASSED);
		System.out.printf("Total %d test(s)", total_tests);
		color(DEFAULT);
		if (total_fatal > 0 || total_suspended > 0 || total_pending > 0 || total_failing > 0 || total_passing > 0) {
			System.out.print(" (");
			if (total_fatal > 0) record(total_fatal, "fatal", FATAL);
			if (total_suspended > 0) recordSuspended();
			if (total_pending > 0) recordPending();
			if (total_failing > 0) recordFailing();
			if (total_passing > 0) recordPassing();
			color(DEFAULT);
			System.out.printf(")");
		}
		System.out.printf("\n");
	}

}
