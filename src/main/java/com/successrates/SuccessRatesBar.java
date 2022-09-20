package com.successrates;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.components.ProgressBar;

class SuccessRatesBar extends JPanel
{
	private int successes;
	private int failures;

	private final int level;

	private final ProgressBar progressBar = new ProgressBar();

	SuccessRatesBar(int level, Color color)
	{
		this.level = level;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 2, 0));

		progressBar.setBackground(new Color(61, 56, 49));
		progressBar.setForeground(color);

		progressBar.setCenterLabel("" + level);

		updateInfo();

		add(progressBar);
	}

	private void updateInfo()
	{
		progressBar.setMaximumValue(getTotal());
		progressBar.setValue(successes);
		progressBar.setLeftLabel("" + successes);
		progressBar.setRightLabel("" + failures);

		progressBar.setToolTipText("<html>" +
			"Level: " + level + "<br>" +
			"Successes: " + String.format("%.2f", getSuccessPercentage()) + " %" + "<br>" +
			"Failures: " + String.format("%.2f", getFailurePercentage()) + " %" + "</html>");

		setVisible(!isEmpty());
	}

	private boolean isEmpty()
	{
		return getTotal() <= 0;
	}

	private int getTotal()
	{
		return successes + failures;
	}

	private double getSuccessPercentage()
	{
		return 100.0 * successes / getTotal();
	}

	private double getFailurePercentage()
	{
		return 100.0 * failures / getTotal();
	}

	public void update(int deltaSuccesses, int deltaFailures)
	{
		successes += deltaSuccesses;
		failures += deltaFailures;

		updateInfo();
	}

	@Override
	public String toString()
	{
		return level + "\t" + successes + "\t" + failures;
	}
}
