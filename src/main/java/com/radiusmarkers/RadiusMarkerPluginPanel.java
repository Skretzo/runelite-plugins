package com.radiusmarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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

public class RadiusMarkerPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private final JLabel markerAdd = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final PluginErrorPanel noMarkersPanel = new PluginErrorPanel();
	private final JPanel markerView = new JPanel();

	private final RadiusMarkerPlugin plugin;
	private final RadiusMarkerConfig config;

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	public RadiusMarkerPluginPanel(RadiusMarkerPlugin radiusMarkerPlugin, RadiusMarkerConfig config)
	{
		this.plugin = radiusMarkerPlugin;
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		title.setText("Radius Markers");
		title.setForeground(Color.WHITE);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(markerAdd, BorderLayout.EAST);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		markerView.setLayout(new BoxLayout(markerView, BoxLayout.Y_AXIS));
		markerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		noMarkersPanel.setContent("Radius Markers", "Highlight radius regions in the game scene.");
		noMarkersPanel.setVisible(false);

		markerView.add(noMarkersPanel);

		markerAdd.setToolTipText("Add new radius marker");
		markerAdd.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				addMarker();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				markerAdd.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				markerAdd.setIcon(ADD_ICON);
			}
		});

		centerPanel.add(markerView, BorderLayout.NORTH);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		markerView.removeAll();

		for (final ColourRadiusMarker marker : plugin.getMarkers())
		{
			markerView.add(new RadiusMarkerPanel(plugin, config, marker));
			markerView.add(Box.createRigidArea(new Dimension(0, 10)));
		}

		boolean empty = markerView.getComponentCount() == 0;
		noMarkersPanel.setVisible(empty);
		title.setVisible(!empty);

		markerView.add(noMarkersPanel);

		repaint();
		revalidate();
	}

	private void addMarker()
	{
		noMarkersPanel.setVisible(false);
		title.setVisible(true);

		plugin.addMarker();
	}
}
