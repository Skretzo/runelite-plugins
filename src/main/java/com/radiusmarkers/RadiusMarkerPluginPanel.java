package com.radiusmarkers;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

class RadiusMarkerPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final ImageIcon[] FILTER_ICONS;
	private static final String[] FILTER_TEXT = {"ALL", "R", "", ""};
	private static final String[] FILTER_DESCRIPTIONS;

	private final JLabel markerAdd = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final JLabel filter = new JLabel(FILTER_ICONS[0]);
	private final IconTextField searchBar = new IconTextField();
	private final PluginErrorPanel noMarkersPanel = new PluginErrorPanel();
	private final JPanel markerView = new JPanel();
	private final JPanel searchPanel = new JPanel(new BorderLayout());

	private final Client client;
	private final RadiusMarkerPlugin plugin;
	private final RadiusMarkerConfig config;

	private PanelFilter panelFilter = PanelFilter.ALL;

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "visible_icon.png");
		final BufferedImage invisibleImg = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "invisible_icon.png");
		final BufferedImage regionIcon = ImageUtil.loadImageResource(RadiusMarkerPlugin.class, "region_icon.png");

		FILTER_ICONS = new ImageIcon[]
		{
			new ImageIcon(ImageUtil.alphaOffset(visibleImg, 0.0f)),
			new ImageIcon(regionIcon),
			new ImageIcon(visibleImg),
			new ImageIcon(invisibleImg)
		};

		FILTER_DESCRIPTIONS = new String[]
		{
			"<html>Filter:<br>Listing all markers</html>",
			"<html>Filter:<br>Listing only markers in the current region</html>",
			"<html>Filter:<br>Listing only visible markers</html>",
			"<html>Filter:<br>Listing only hidden markers</html>"
		};
	}

	public RadiusMarkerPluginPanel(Client client, RadiusMarkerPlugin plugin, RadiusMarkerConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(1, 3, 10, 7));

		searchPanel.setBorder(new EmptyBorder(1, 0, 0, 0));

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARKER_GRAY_HOVER_COLOR);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 43 - filter.getWidth(), 24));
		searchBar.addActionListener(e -> rebuild());
		searchBar.addClearListener(this::rebuild);
		searchBar.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					searchBar.setText("");
					rebuild();
				}
			}
		});

		filter.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filter.setOpaque(true);
		filter.setPreferredSize(new Dimension(28, 24));
		filter.setText(FILTER_TEXT[0]);
		filter.setToolTipText(FILTER_DESCRIPTIONS[0]);
		filter.setHorizontalTextPosition(JLabel.CENTER);
		filter.setFont(FontManager.getRunescapeSmallFont());
		filter.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				panelFilter = panelFilter.next();
				filter.setText(FILTER_TEXT[panelFilter.ordinal()]);
				filter.setIcon(FILTER_ICONS[panelFilter.ordinal()]);
				filter.setToolTipText(FILTER_DESCRIPTIONS[panelFilter.ordinal()]);
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				filter.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				filter.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		title.setText("Radius Markers");
		title.setForeground(Color.WHITE);

		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(markerAdd, BorderLayout.EAST);

		searchPanel.add(searchBar, BorderLayout.WEST);
		searchPanel.add(filter, BorderLayout.EAST);

		northPanel.add(titlePanel, BorderLayout.NORTH);
		northPanel.add(searchPanel, BorderLayout.CENTER);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		markerView.setLayout(new BoxLayout(markerView, BoxLayout.Y_AXIS));
		markerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

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

		int regionId = client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getWorldLocation().getRegionID();

		for (final ColourRadiusMarker marker : plugin.getMarkers())
		{
			if (marker.getName().contains(searchBar.getText()) &&
				(PanelFilter.ALL.equals(panelFilter) ||
				(PanelFilter.REGION.equals(panelFilter) && marker.getWorldPoint().getRegionID() == regionId) ||
				(PanelFilter.VISIBLE.equals(panelFilter) && marker.isVisible()) ||
				(PanelFilter.INVISIBLE.equals(panelFilter) && !marker.isVisible())))
			{
				markerView.add(new RadiusMarkerPanel(plugin, config, marker));
				markerView.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		}

		boolean empty = markerView.getComponentCount() == 0;
		noMarkersPanel.setContent("Radius Markers",
			"Click the '+' button to add a region marker at the feet of your character.");
		noMarkersPanel.setVisible(empty);
		searchPanel.setVisible(!empty);
		if (empty && plugin.getMarkers().size() > 0)
		{
			noMarkersPanel.setContent("Radius Markers",
				"No radius markers are available for the current search term and/or selected filter.");
			searchPanel.setVisible(true);
		}

		markerView.add(noMarkersPanel);

		repaint();
		revalidate();
	}

	private void addMarker()
	{
		noMarkersPanel.setVisible(false);
		searchPanel.setVisible(true);
		final ColourRadiusMarker marker = plugin.addMarker();
		SwingUtilities.invokeLater(() ->
		{
			if (marker != null && marker.getPanel() != null)
			{
				Rectangle markerPosition = marker.getPanel().getBounds();
				markerPosition.setLocation((int) markerPosition.getX(), (int) markerPosition.getY() - 10 -
					(int) markerPosition.getHeight() / 2 + ((Applet) client).getParent().getHeight());
				scrollRectToVisible(markerPosition);
			}
		});
	}
}
