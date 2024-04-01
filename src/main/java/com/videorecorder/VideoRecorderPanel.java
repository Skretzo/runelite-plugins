package com.videorecorder;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
	VideoRecorderPanel(VideoRecorderPlugin plugin, VideoRecorderConfig config)
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
		bottomPanel.setLayout(new BorderLayout());

		final JPanel warningPanel = new JPanel();
		warningPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		warningPanel.setLayout(new BorderLayout());
		warningPanel.setBackground(Color.RED.darker().darker().darker());

		final JLabel warningText = new JLabel();
		warningText.setBorder(new EmptyBorder(0, 0, 5, 0));
		warningText.setForeground(Color.WHITE);
		warningText.setHorizontalAlignment(SwingConstants.CENTER);
		warningText.setText("<html><body style = 'text-align:center'>" +
			"Warning<br>Recording with this plugin while using the GPU plugin or 117HD plugin may cause your client to crash." +
			"</body></html>");

		final JButton warningButton = new JButton("Remove warning");
		warningButton.setBackground(warningPanel.getBackground().darker());
		warningButton.addActionListener(e ->
		{
			config.setShowWarning(false);
			warningPanel.setVisible(false);
		});

		warningPanel.add(warningText, BorderLayout.NORTH);
		warningPanel.add(warningButton, BorderLayout.CENTER);

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
		bottomPanel.add(outputButton, BorderLayout.NORTH);
		if (config.showWarning())
		{
			bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
			bottomPanel.add(warningPanel, BorderLayout.SOUTH);
		}

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
