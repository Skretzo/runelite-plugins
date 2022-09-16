package com.damagetracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

class DamageTrackerPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private final DamageTrackerPlugin plugin;
	private final JPanel trackerView = new JPanel();
	private final List<DamageTrackerPanel> panels = new ArrayList<>();
	private final PluginErrorPanel noTrackersPanel = new PluginErrorPanel();


	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(DamageTrackerPlugin.class, "/add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	public DamageTrackerPluginPanel(DamageTrackerPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(DamageTracker.class.getSimpleName()), " "));
		title.setForeground(Color.WHITE);

		JLabel addButton = new JLabel(ADD_ICON);
		addButton.setToolTipText("Add a new damage tracker");
		addButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				addTracker();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addButton.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addButton.setIcon(ADD_ICON);
			}
		});

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(1, 3, 10, 0));
		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(addButton, BorderLayout.EAST);

		trackerView.setLayout(new BoxLayout(trackerView, BoxLayout.Y_AXIS));
		trackerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		rebuild();

		add(titlePanel, BorderLayout.NORTH);
		add(trackerView, BorderLayout.CENTER);
	}

	private void addTracker()
	{
		noTrackersPanel.setVisible(false);
		plugin.saveTracker(plugin.addTracker());
		rebuild();
	}

	public void removeTracker(DamageTrackerPanel trackerPanel)
	{
		plugin.removeTracker(trackerPanel.getTracker());
		rebuild();
	}

	public void update()
	{
		for (DamageTrackerPanel panel : panels)
		{
			panel.updateBars();
		}

		repaint();
		revalidate();
	}

	public void rebuild()
	{
		panels.clear();
		trackerView.removeAll();

		List<DamageTracker> trackers = plugin.getTrackers();
		Collections.sort(trackers);

		for (DamageTracker tracker : trackers)
		{
			DamageTrackerPanel panel = new DamageTrackerPanel(plugin, this, tracker);
			panels.add(panel);
			trackerView.add(panel);
			trackerView.add(Box.createRigidArea(new Dimension(0, 10)));
		}

		noTrackersPanel.setContent(
			StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(DamageTracker.class.getSimpleName()), " "),
			"Click the '+' button to add a new damage tracker");
		noTrackersPanel.setVisible(trackerView.getComponentCount() == 0);
		trackerView.add(noTrackersPanel);

		repaint();
		revalidate();
	}
}
