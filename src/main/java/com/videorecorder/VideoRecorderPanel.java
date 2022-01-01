package com.videorecorder;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

public class VideoRecorderPanel extends PluginPanel
{
	private final JButton startButton = new JButton("Start");
	private final JButton stopButton = new JButton("Stop");
	private final VideoRecorderPlugin plugin;

	@Inject
	VideoRecorderPanel(VideoRecorderPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(0, 1, 3, 10));

		final JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new EmptyBorder(10, 0, 1, 0));

		final JLabel title = new JLabel(VideoRecorderPlugin.VIDEO_RECORDER);
		title.setForeground(Color.WHITE);

		startButton.setPreferredSize(new Dimension(0, 40));
		startButton.addActionListener(e -> toggleVideo(true));

		stopButton.addActionListener(e -> toggleVideo(false));
		stopButton.setEnabled(false);

		final JButton outputButton = new JButton("Open output folder");
		outputButton.addActionListener(e -> LinkBrowser.open(VideoRecorderPlugin.VIDEO_DIR.toString()));

		topPanel.add(title);
		centerPanel.add(startButton);
		centerPanel.add(stopButton);
		bottomPanel.add(outputButton);

		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	public void toggleVideo(boolean start)
	{
		plugin.toggleVideo(start);
		startButton.setEnabled(!start);
		stopButton.setEnabled(start);
	}
}
