package se.alanif.jregr.gui;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.cli.CommandLine;

import se.alanif.jregr.exec.RegrCase;
import se.alanif.jregr.exec.RegrCase.State;
import se.alanif.jregr.reporters.RegrReporter;

public class GuiReporter implements RegrReporter {

	private DefaultListModel<RegrCase> model = new DefaultListModel<RegrCase>();
	private RegrCaseListView regrCaseListView;
	private JPanel panel = new JPanel();

	public GuiReporter() {
		regrCaseListView = new RegrCaseListView(model);
		panel.add(new JScrollPane(regrCaseListView));
		panel.setSize(600, 600);
	}
	
	public JPanel getView() {
		return panel;
	}
	
	public void start(String suite, int numberOfTests, CommandLine commandLine) {
	}

	
	public void starting(RegrCase theCase, long millis) {
		model.add(0, theCase);
	}

	public void fatal() {
	}

	public void virgin() {
	}

	public void pending() {
	}

	public void pass() {
	}

	public void fail() {
	}

	public void suspended() {
	}

	public void end() {
	}

	public void suspendedAndFailed() {
	}

	public void suspendedAndPassed() {
	}

	public void report(State status) {
	}

}
