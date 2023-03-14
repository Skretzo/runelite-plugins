package com.linemarkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

class LineMarkerPluginPanel extends PluginPanel
{
	private static final ImageIcon COPY_ICON;
	private static final ImageIcon COPY_HOVER_ICON;
	private static final ImageIcon PASTE_ICON;
	private static final ImageIcon PASTE_HOVER_ICON;

	private static final ImageIcon[] FILTER_ICONS;
	private static final String[] FILTER_TEXT = {"ALL", "R", "", ""};
	private static final String[] FILTER_DESCRIPTIONS;

	private final JLabel copyMarkers = new JLabel(COPY_ICON);
	private final JLabel pasteMarkers = new JLabel(PASTE_ICON);
	private final JLabel filterButton = new JLabel(FILTER_ICONS[0]);
	private final IconTextField searchBar = new IconTextField();
	private final PluginErrorPanel noMarkersPanel = new PluginErrorPanel();
	private final JPanel markerView = new JPanel();
	private final JPanel searchPanel = new JPanel(new BorderLayout());

	private final Client client;
	private final LineMarkerPlugin plugin;
	private final LineMarkerConfig config;

	@Getter
	private Filter filter = Filter.ALL;

	static
	{
		final BufferedImage copyIcon = ImageUtil.loadImageResource(LineMarkerPlugin.class, "copy_icon.png");
		COPY_ICON = new ImageIcon(copyIcon);
		COPY_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(copyIcon, 0.53f));

		final BufferedImage pasteIcon = ImageUtil.loadImageResource(LineMarkerPlugin.class, "paste_icon.png");
		PASTE_ICON = new ImageIcon(pasteIcon);
		PASTE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(pasteIcon, 0.53f));

		final BufferedImage visibleImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "visible_icon.png");
		final BufferedImage invisibleImg = ImageUtil.loadImageResource(LineMarkerPlugin.class, "invisible_icon.png");
		final BufferedImage regionIcon = ImageUtil.loadImageResource(LineMarkerPlugin.class, "region_icon.png");

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

	public LineMarkerPluginPanel(Client client, LineMarkerPlugin plugin, LineMarkerConfig config)
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

		JPanel markerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 3));

		searchPanel.setBorder(new EmptyBorder(1, 0, 0, 0));

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARKER_GRAY_HOVER_COLOR);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 43 - filterButton.getWidth(), 24));
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

		filterButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterButton.setOpaque(true);
		filterButton.setPreferredSize(new Dimension(28, 24));
		filterButton.setText(FILTER_TEXT[0]);
		filterButton.setToolTipText(FILTER_DESCRIPTIONS[0]);
		filterButton.setHorizontalTextPosition(JLabel.CENTER);
		filterButton.setFont(FontManager.getRunescapeSmallFont());
		filterButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				filter = filter.next();
				filterButton.setText(FILTER_TEXT[filter.ordinal()]);
				filterButton.setIcon(FILTER_ICONS[filter.ordinal()]);
				filterButton.setToolTipText(FILTER_DESCRIPTIONS[filter.ordinal()]);
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				filterButton.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				filterButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		JLabel title = new JLabel();
		title.setText("Line Markers");
		title.setForeground(Color.WHITE);

		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(markerButtons, BorderLayout.EAST);

		searchPanel.add(searchBar, BorderLayout.WEST);
		searchPanel.add(filterButton, BorderLayout.EAST);

		northPanel.add(titlePanel, BorderLayout.NORTH);
		northPanel.add(searchPanel, BorderLayout.CENTER);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		markerView.setLayout(new BoxLayout(markerView, BoxLayout.Y_AXIS));
		markerView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		noMarkersPanel.setVisible(false);

		markerView.add(noMarkersPanel);

		copyMarkers.setToolTipText("Export all searched or filtered markers to your clipboard");
		copyMarkers.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				copyMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				copyMarkers.setIcon(COPY_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				copyMarkers.setIcon(COPY_ICON);
			}
		});

		pasteMarkers.setToolTipText("Import markers from your clipboard");
		pasteMarkers.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				pasteMarkers();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				pasteMarkers.setIcon(PASTE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				pasteMarkers.setIcon(PASTE_ICON);
			}
		});

		markerButtons.add(pasteMarkers);
		markerButtons.add(copyMarkers);

		centerPanel.add(markerView, BorderLayout.NORTH);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		markerView.removeAll();

		int regionId = client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getWorldLocation().getRegionID();

		plugin.getGroups().sort(Comparator.comparing(LineGroup::getName));
		for (final LineGroup group : plugin.getGroups())
		{
			if (group.getName().toLowerCase().contains(getSearchText().toLowerCase()) &&
				(Filter.ALL.equals(filter) ||
				(Filter.REGION.equals(filter) && plugin.anyLineInRegion(group.getLines(), regionId)) ||
				(Filter.VISIBLE.equals(filter) && group.isVisible()) ||
				(Filter.INVISIBLE.equals(filter) && !group.isVisible())))
			{
				markerView.add(new LineMarkerPanel(plugin, config, group));
				markerView.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		}

		boolean empty = markerView.getComponentCount() == 0;
		noMarkersPanel.setContent("Line Markers", "Shift right-click a tile to add a line marker.");
		noMarkersPanel.setVisible(empty);
		searchPanel.setVisible(!empty);
		if (empty && plugin.getGroups().size() > 0)
		{
			noMarkersPanel.setContent("Line Markers",
				"No line markers are available for the current search term and/or selected filter.");
			searchPanel.setVisible(true);
		}

		markerView.add(noMarkersPanel);

		repaint();
		revalidate();
	}

	public String getSearchText()
	{
		return searchBar.getText();
	}

	private void copyMarkers()
	{
		if (plugin.copyMarkers() != null)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(plugin.copyMarkers()), null);
		}
	}

	private void pasteMarkers()
	{
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
		}
		catch (IOException | UnsupportedFlavorException ignore)
		{
			return;
		}

		if (plugin.pasteMarkers(clipboardText))
		{
			noMarkersPanel.setVisible(false);
			searchPanel.setVisible(true);
			rebuild();
		}
	}
}
