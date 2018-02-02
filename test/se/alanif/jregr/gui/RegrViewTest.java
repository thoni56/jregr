package se.alanif.jregr.gui;

import javax.swing.JPanel;

import junit.extensions.abbot.ComponentTestFixture;

public class RegrViewTest extends ComponentTestFixture {

	private RegrView regrView;
	private GuiTestUtilities gui;
	private JPanel injectedResultsPanel;

	private RegrView givenARegrView() {
		injectedResultsPanel = new JPanel();
		return new RegrView(injectedResultsPanel);
	}

	private GuiTestUtilities showGui(RegrView view) {
		showWindow(view);
		GuiTestUtilities gui = new GuiTestUtilities(getFinder(), view.getContentPane());
		return gui;
	}

	protected void setUp() {
		regrView = givenARegrView();
		gui = showGui(regrView);
	}

	public void testHasRegrWindow() throws Exception {
		gui.findJPanel("JRegr");
	}

	public void testHasRunButton() throws Exception {
		gui.findButton("Run");
	}

	public void testAddsTheInjectedResultsPanel() throws Exception {
		assertEquals(injectedResultsPanel, gui.findJPanel("Results"));
	}

}
