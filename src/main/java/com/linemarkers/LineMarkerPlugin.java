package com.linemarkers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.SpriteID;
import net.runelite.api.Tile;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Line Markers",
	description = "Draw lines on tiles",
	tags = {"line", "tile", "edge", "ground", "marker"}
)
public class LineMarkerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "linemarkers";
	private static final String CONFIG_KEY = "markers";
	private static final String PLUGIN_NAME = "Line Markers";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String DEFAULT_MARKER_NAME = "Marker";
	private static final String ADD_LINE = "Add line";
	private static final String REMOVE_LINE = "Remove line";
	private static final String WALK_HERE = "Walk here";

	@Getter(AccessLevel.PACKAGE)
	private List<LineGroup> groups = new ArrayList<>();

	/**
	 * 'markers' is a mirror of 'groups' with additional copies of LineGroups for all instances
	 */
	@Getter(AccessLevel.PACKAGE)
	private List<LineGroup> markers = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private LineMarkerConfig config;

	@Inject
	private LineMarkerMapOverlay mapOverlay;

	@Inject
	private LineMarkerSceneOverlay sceneOverlay;

	@Inject
	private LineMarkerMinimapOverlay minimapOverlay;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Gson gson;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Getter
	@Inject
	private ColorPickerManager colourPickerManager;

	private LineMarkerPluginPanel pluginPanel;
	private NavigationButton navigationButton;
	private BufferedImage minimapSpriteFixed;
	private BufferedImage minimapSpriteResizeable;
	private Shape minimapClipFixed;
	private Shape minimapClipResizeable;
	private Rectangle minimapRectangle = new Rectangle();
	private LineGroup lastGroup = null;
	private Line lastLine = null;
	private boolean isHotkeyPressed = false;

	private HotkeyListener hotkeyListener = new HotkeyListener(() -> Keybind.SHIFT)
	{
		@Override
		public void hotkeyPressed()
		{
			isHotkeyPressed = true;
		}

		@Override
		public void hotkeyReleased()
		{
			lastLine = null;
			lastGroup = null;
			isHotkeyPressed = false;
		}
	};

	private MouseWheelListener mouseWheelListener = event ->
	{
		if (isHotkeyPressed && lastLine != null)
		{
			if (event.getWheelRotation() > 0)
			{
				lastLine.setEdge(lastLine.getEdge().next());
			}
			else
			{
				lastLine.setEdge(lastLine.getEdge().next().next().next());
			}

			event.consume();
			saveMarkers();
			revalidate();
		}
		return event;
	};

	@Provides
	LineMarkerConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LineMarkerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(hotkeyListener);
		mouseManager.registerMouseWheelListener(mouseWheelListener);

		overlayManager.add(mapOverlay);
		overlayManager.add(sceneOverlay);
		overlayManager.add(minimapOverlay);

		loadMarkers();

		pluginPanel = new LineMarkerPluginPanel(client, this, config);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(hotkeyListener);
		mouseManager.unregisterMouseWheelListener(mouseWheelListener);

		overlayManager.remove(mapOverlay);
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(minimapOverlay);

		groups.clear();
		markers.clear();

		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!GameState.LOGGED_IN.equals(event.getGameState()) || !client.isInInstancedRegion())
		{
			return;
		}

		mirrorMarkers();
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (isHotkeyPressed && event.getOption().equals(WALK_HERE) && event.getTarget().isEmpty())
		{
			client.createMenuEntry(1)
				.setOption(ADD_LINE)
				.setTarget(event.getTarget())
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setIdentifier(event.getIdentifier())
				.setType(MenuAction.RUNELITE)
				.onClick(this::addMarker);

			Line line = getLineOnTile();
			if (line != null)
			{
				client.createMenuEntry(1)
					.setOption(REMOVE_LINE)
					.setTarget(event.getTarget())
					.setParam0(event.getActionParam0())
					.setParam1(event.getActionParam1())
					.setIdentifier(event.getIdentifier())
					.setType(MenuAction.RUNELITE)
					.onClick(e -> removeMarker(line));
			}
		}
	}

	private void addMarker(MenuEntry entry)
	{
		Tile tile = client.getSelectedSceneTile();
		if (tile == null)
		{
			return;
		}

		lastLine = new Line(config, WorldPoint.fromLocalInstance(client, tile.getLocalLocation()));

		if (lastGroup == null)
		{
			lastGroup = new LineGroup(DEFAULT_MARKER_NAME + " " + (groups.size() + 1), lastLine);
			groups.add(lastGroup);
		}
		else
		{
			lastGroup.getLines().add(lastLine);
		}

		saveMarkers();
		rebuild();
	}

	private void loadMarkers()
	{
		groups.clear();
		markers.clear();

		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);

		if (Strings.isNullOrEmpty(json))
		{
			return;
		}

		try
		{
			groups = gson.fromJson(json, new TypeToken<ArrayList<LineGroup>>(){}.getType());
			mirrorMarkers();
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
			JOptionPane.showConfirmDialog(pluginPanel,
				"The line markers you are trying to load from your config are malformed",
				"Warning", JOptionPane.OK_CANCEL_OPTION);
		}
	}

	public void saveMarkers()
	{
		if (groups == null || groups.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			return;
		}

		String json = gson.toJson(groups);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
		mirrorMarkers();
	}

	private void mirrorMarkers()
	{
		markers = LineGroup.instances(client, groups);
	}

	public String copyMarkers()
	{
		List<LineGroup> markersCopy = new ArrayList<>();

		int regionId = client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getWorldLocation().getRegionID();
		String searchTerm = pluginPanel.getSearchText().toLowerCase();
		Filter filter = pluginPanel.getFilter();

		for (LineGroup group : groups)
		{
			if (group.getName().toLowerCase().contains(searchTerm) &&
				(Filter.ALL.equals(filter) ||
				(Filter.REGION.equals(filter) && anyLineInRegion(group.getLines(), regionId)) ||
				(Filter.VISIBLE.equals(filter) && group.isVisible()) ||
				(Filter.INVISIBLE.equals(filter) && !group.isVisible())))
			{
				markersCopy.add(group);
			}
		}

		if (markersCopy.isEmpty())
		{
			return null;
		}

		return gson.toJson(markersCopy);
	}

	public boolean pasteMarkers(String json)
	{
		if (Strings.isNullOrEmpty(json))
		{
			return false;
		}

		List<LineGroup> markers;
		try
		{
			markers = gson.fromJson(json, new TypeToken<ArrayList<LineGroup>>(){}.getType());
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
			JOptionPane.showConfirmDialog(pluginPanel,
				"The line markers you are trying to import are malformed",
				"Warning", JOptionPane.OK_CANCEL_OPTION);
			return false;
		}

		for (LineGroup marker : markers)
		{
			if (!groups.contains(marker))
			{
				groups.add(marker);
			}
		}

		saveMarkers();

		return true;
	}

	public boolean anyLineInRegion(List<Line> lines, int regionId)
	{
		for (Line line : lines)
		{
			if (line.getLocation().getRegionID() == regionId)
			{
				return true;
			}
		}
		return false;
	}

	private Line getLineOnTile()
	{
		Tile tile = client.getSelectedSceneTile();
		if (tile == null)
		{
			return null;
		}

		for (LineGroup group : groups)
		{
			for (Line line : group.getLines())
			{
				if (line.getLocation().equals(tile.getWorldLocation()))
				{
					return line;
				}
			}
		}

		return null;
	}

	private Polygon bufferedImageToPolygon(BufferedImage image)
	{
		int outsideColour = -1;
		int previousColour;
		final int width = image.getWidth();
		final int height = image.getHeight();
		List<Point> points = new ArrayList<>();
		for (int y = 0; y < height; y++)
		{
			previousColour = outsideColour;
			for (int x = 0; x < width; x++)
			{
				int colour = image.getRGB(x, y);
				if (x == 0 && y == 0)
				{
					outsideColour = colour;
					previousColour = colour;
				}
				if (colour != outsideColour && previousColour == outsideColour)
				{
					points.add(new Point(x, y));
				}
				if ((colour == outsideColour || x == (width - 1)) && previousColour != outsideColour)
				{
					points.add(0, new Point(x, y));
				}
				previousColour = colour;
			}
		}
		int offsetX = 0;
		int offsetY = 0;
		Widget minimapDrawWidget = getMinimapDrawWidget();
		if (minimapDrawWidget != null)
		{
			offsetX = minimapDrawWidget.getBounds().x;
			offsetY = minimapDrawWidget.getBounds().y;
		}
		Polygon polygon = new Polygon();
		for (Point point : points)
		{
			polygon.addPoint(point.x + offsetX, point.y + offsetY);
		}
		return polygon;
	}

	private Shape getMinimapClipAreaSimple()
	{
		Widget minimapDrawArea = getMinimapDrawWidget();

		if (minimapDrawArea == null || minimapDrawArea.isHidden())
		{
			return null;
		}

		Rectangle bounds = minimapDrawArea.getBounds();

		return new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	public void removeMarker(Line line)
	{
		for (LineGroup group : groups)
		{
			group.getLines().remove(line);
			if (group.getLines().isEmpty())
			{
				groups.remove(group);
				break;
			}
		}

		saveMarkers();
		rebuild();
	}

	public void removeMarker(LineGroup group)
	{
		groups.remove(group);
		saveMarkers();
		rebuild();
	}

	public void rebuild()
	{
		SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
	}

	public void revalidate()
	{
		SwingUtilities.invokeLater(() -> pluginPanel.revalidate());
	}

	public Shape getMinimapClipArea()
	{
		Widget minimapWidget = getMinimapDrawWidget();

		if (minimapWidget == null || minimapWidget.isHidden() || !minimapRectangle.equals(minimapRectangle = minimapWidget.getBounds()))
		{
			minimapClipFixed = null;
			minimapClipResizeable = null;
			minimapSpriteFixed = null;
			minimapSpriteResizeable = null;
		}

		if (client.isResized())
		{
			if (minimapClipResizeable != null)
			{
				return minimapClipResizeable;
			}
			if (minimapSpriteResizeable == null)
			{
				minimapSpriteResizeable = spriteManager.getSprite(SpriteID.RESIZEABLE_MODE_MINIMAP_ALPHA_MASK, 0);
			}
			if (minimapSpriteResizeable != null)
			{
				return minimapClipResizeable = bufferedImageToPolygon(minimapSpriteResizeable);
			}
			return getMinimapClipAreaSimple();
		}
		if (minimapClipFixed != null)
		{
			return minimapClipFixed;
		}
		if (minimapSpriteFixed == null)
		{
			minimapSpriteFixed = spriteManager.getSprite(SpriteID.FIXED_MODE_MINIMAP_ALPHA_MASK, 0);
		}
		if (minimapSpriteFixed != null)
		{
			return minimapClipFixed = bufferedImageToPolygon(minimapSpriteFixed);
		}
		return getMinimapClipAreaSimple();
	}

	public Widget getMinimapDrawWidget()
	{
		if (client.isResized())
		{
			if (client.getVarbitValue(Varbits.SIDE_PANELS) == 1)
			{
				return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
			}
			return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
		}
		return client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
	}
}
