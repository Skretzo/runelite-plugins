package com.damagetracker;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.components.ProgressBar;

class DamageTrackerBar extends JPanel implements Comparable<DamageTrackerBar>
{
	private final int damage;
	private final DamageTracker tracker;
	private final ProgressBar counterBar = new ProgressBar();

	@Getter(AccessLevel.PRIVATE)
	private int count;

	DamageTrackerBar(int damage, int count, DamageTracker tracker)
	{
		this.damage = damage;
		this.count = count;
		this.tracker = tracker;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 2, 0));

		counterBar.setBackground(new Color(61, 56, 49));
		counterBar.setForeground(damage == 0 ? new Color(0, 150, 255) : new Color(255, 100, 100));

		counterBar.setLeftLabel(Integer.toString(damage));

		updateInfo();

		add(counterBar);
	}

	public void updateInfo()
	{
		int maximum = tracker.getMaximum();
		int total = tracker.getTotal();
		double average = tracker.getAverage();
		double percentage = total <= 0 ? -1 : (100.0 * count) / total;

		updateScale(maximum);
		updateTooltipText(total, percentage, average);

		counterBar.setValue(count);
		counterBar.setCenterLabel(Integer.toString(count));

		for (DamageTrackerBar bar : tracker.getTrackerBars().values())
		{
			percentage = total <= 0 ? -1 : (100.0 * bar.getCount()) / total;
			bar.updateScale(maximum);
			bar.updateTooltipText(total, percentage, average);
		}

		setVisible(!isEmpty());
	}

	public int update()
	{
		return ++count;
	}

	private void updateScale(int maximum)
	{
		counterBar.setMaximumValue(Math.max(maximum, 1));
	}

	private void updateTooltipText(int total, double percentage, double average)
	{
		counterBar.setToolTipText("<html>" +
			"Damage: " + damage + "<br>" +
			"Count: " + count + " (" + String.format("%.2f", percentage) + " %)<br>" +
			"Total: " + total + "<br>" +
			"Average: " + String.format("%.3f", average) + "</html>");
	}

	private boolean isEmpty()
	{
		return count <= 0;
	}

	@Override
	public int compareTo(DamageTrackerBar other)
	{
		return Integer.compare(damage, other.damage);
	}

	@Override
	public String toString()
	{
		return damage + "\t" + count;
	}
}
