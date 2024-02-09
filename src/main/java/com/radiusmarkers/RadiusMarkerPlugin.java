package com.radiusmarkers;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Radius Markers",
	description = "Highlight NPC radius regions like attack, hunt, max and wander range",
	tags = {"npc", "range", "region", "aggression", "attack", "hunt", "interaction", "max", "retreat", "wander"}
)
public class RadiusMarkerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "radiusmarkers";
	private static final String CONFIG_KEY = "markers";
	private static final String PLUGIN_NAME = "Radius Markers";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String DEFAULT_MARKER_NAME = "Marker";
	private static final String UPDATE_MARKER = "Update marker";

	@Getter(AccessLevel.PACKAGE)
	private final List<ColourRadiusMarker> markers = new ArrayList<>();

	@Setter(AccessLevel.PACKAGE)
	private ColourRadiusMarker renameMarker = null;

	@Inject
	private Client client;

	@Inject
	private RadiusMarkerConfig config;

	@Inject
	private RadiusMarkerMapOverlay mapOverlay;

	@Inject
	private RadiusMarkerSceneOverlay sceneOverlay;

	@Inject
	private RadiusMarkerMinimapOverlay minimapOverlay;

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

	@Getter
	@Inject
	private ColorPickerManager colourPickerManager;

	private RadiusMarkerPluginPanel pluginPanel;
	private NavigationButton navigationButton;
	private BufferedImage minimapSpriteFixed;
	private BufferedImage minimapSpriteResizeable;
	private Shape minimapClipFixed;
	private Shape minimapClipResizeable;
	private Rectangle minimapRectangle = new Rectangle();

	@Provides
	RadiusMarkerConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RadiusMarkerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(mapOverlay);
		overlayManager.add(sceneOverlay);
		overlayManager.add(minimapOverlay);

		loadMarkers();

		pluginPanel = new RadiusMarkerPluginPanel(client, this, config);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();


		if (!config.hideNavButton())
		{
			clientToolbar.addNavigation(navigationButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(mapOverlay);
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(minimapOverlay);

		markers.clear();

		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}
		if (configChanged.getKey().equals("hideNavButton"))
		{
			if (config.hideNavButton())
			{
				clientToolbar.removeNavigation(navigationButton);
			}
			else
			{
				clientToolbar.addNavigation(navigationButton);
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		int type = event.getType();

		if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
		}

		final MenuAction menuAction = MenuAction.of(type);

		if (MenuAction.EXAMINE_NPC.equals(menuAction) && renameMarker != null)
		{
			final int id = event.getIdentifier();
			final NPC[] cachedNPCs = client.getCachedNPCs();
			final NPC npc = cachedNPCs[id];

			if (npc == null || npc.getName() == null)
			{
				return;
			}

			client.createMenuEntry(-1)
				.setOption(UPDATE_MARKER)
				.setTarget(event.getTarget())
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setIdentifier(event.getIdentifier())
				.setType(MenuAction.RUNELITE)
				.onClick(this::updateMarkerInfo);
		}
	}

	private void updateMarkerInfo(MenuEntry entry)
	{
		final NPC[] cachedNPCs = client.getCachedNPCs();
		final NPC npc = cachedNPCs[entry.getIdentifier()];

		if (npc == null || npc.getName() == null || renameMarker == null)
		{
			return;
		}

		renameMarker.getPanel().setMarkerText(npc.getName());
		renameMarker.getPanel().setNpcId(npc.getId());
	}

	private void loadMarkers()
	{
		markers.clear();

		final Collection<RadiusMarker> radiusMarkers = getRadiusMarkers();

		if (radiusMarkers != null)
		{
			final List<ColourRadiusMarker> colourRadiusMarkers = translateToColourRadiusMarker(radiusMarkers);
			markers.addAll(colourRadiusMarkers);
			Collections.sort(markers);
		}
	}

	private Collection<RadiusMarker> getRadiusMarkers()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);

		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		try
		{
			List<RadiusMarker> loaded = gson.fromJson(json, new TypeToken<List<RadiusMarker>>(){}.getType());
			loaded.removeIf(RadiusMarker::isInvalid);
			return loaded;
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
			JOptionPane.showConfirmDialog(pluginPanel,
				"The radius markers you are trying to load from your config are malformed",
				"Warning", JOptionPane.OK_CANCEL_OPTION);
			return null;
		}
	}

	private void saveMarkers(Collection<RadiusMarker> markers)
	{
		if (markers == null || markers.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			return;
		}

		String json = gson.toJson(markers);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	public String copyMarkers()
	{
		List<RadiusMarker> markersCopy = new ArrayList<>();

		int regionId = client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getWorldLocation().getRegionID();
		String searchTerm = pluginPanel.getSearchText().toLowerCase();
		PanelFilter filter = pluginPanel.getPanelFilter();

		for (ColourRadiusMarker marker : markers)
		{
			if (marker.getName().toLowerCase().contains(searchTerm) &&
				(PanelFilter.ALL.equals(filter) ||
				(PanelFilter.REGION.equals(filter) && marker.getWorldPoint().getRegionID() == regionId) ||
				(PanelFilter.VISIBLE.equals(filter) && marker.isVisible()) ||
				(PanelFilter.INVISIBLE.equals(filter) && !marker.isVisible())))
			{
				markersCopy.add(translateToRadiusMarker(marker));
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

		List<RadiusMarker> radiusMarkers;
		try
		{
			radiusMarkers = gson.fromJson(json, new TypeToken<List<RadiusMarker>>(){}.getType());
			radiusMarkers.removeIf(RadiusMarker::isInvalid);
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
			JOptionPane.showConfirmDialog(pluginPanel,
				"The radius markers you are trying to import are malformed",
				"Warning", JOptionPane.OK_CANCEL_OPTION);
			return false;
		}
		List<ColourRadiusMarker> outputMarkers = new ArrayList<>();
		List<ColourRadiusMarker> colourRadiusMarkers = translateToColourRadiusMarker(radiusMarkers);

		for (ColourRadiusMarker radiusMarker : colourRadiusMarkers)
		{
			boolean unique = true;
			for (ColourRadiusMarker marker : markers)
			{
				if (marker.getId() == radiusMarker.getId())
				{
					unique = false;
					break;
				}
			}
			if (unique)
			{
				outputMarkers.add(radiusMarker);
			}
		}

		if (outputMarkers.isEmpty())
		{
			return false;
		}

		markers.addAll(outputMarkers);
		Collections.sort(markers);
		saveMarkers();

		return true;
	}

	public boolean exclude(NPC npc)
	{
		return npc == null || npc.getName() == null || npc.getName().isEmpty() || "null".equals(npc.getName());
	}

	private List<ColourRadiusMarker> translateToColourRadiusMarker(Collection<RadiusMarker> markers)
	{
		if (markers.isEmpty())
		{
			return Collections.emptyList();
		}

		return markers.stream().map(ColourRadiusMarker::new).collect(Collectors.toList());
	}

	private RadiusMarker translateToRadiusMarker(ColourRadiusMarker colourRadiusMarker)
	{
		return new RadiusMarker(
			colourRadiusMarker.getId(),
			colourRadiusMarker.getName(),
			colourRadiusMarker.isVisible(),
			colourRadiusMarker.isCollapsed(),
			colourRadiusMarker.getZ(),
			colourRadiusMarker.getSpawnX(),
			colourRadiusMarker.getSpawnY(),
			colourRadiusMarker.getSpawnColour(),
			colourRadiusMarker.isSpawnVisible(),
			colourRadiusMarker.getWanderRadius(),
			colourRadiusMarker.getWanderColour(),
			colourRadiusMarker.isWanderVisible(),
			colourRadiusMarker.getMaxRadius(),
			colourRadiusMarker.getMaxColour(),
			colourRadiusMarker.isMaxVisible(),
			colourRadiusMarker.getAggressionColour(),
			colourRadiusMarker.isAggressionVisible(),
			colourRadiusMarker.getRetreatInteractionColour(),
			colourRadiusMarker.isRetreatInteractionVisible(),
			colourRadiusMarker.getNpcId(),
			colourRadiusMarker.getAttackRadius(),
			colourRadiusMarker.getAttackColour(),
			colourRadiusMarker.getAttackType(),
			colourRadiusMarker.isAttackVisible(),
			colourRadiusMarker.getHuntRadius(),
			colourRadiusMarker.getHuntColour(),
			colourRadiusMarker.isHuntVisible(),
			colourRadiusMarker.getInteractionRadius(),
			colourRadiusMarker.getInteractionColour(),
			colourRadiusMarker.isInteractionVisible());
	}

	private ColourRadiusMarker findColourRadiusMarker(RadiusMarker radiusMarker)
	{
		for (final ColourRadiusMarker colourRadiusMarker : markers)
		{
			if (colourRadiusMarker.equals(radiusMarker))
			{
				return colourRadiusMarker;
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

	public void saveMarkers()
	{
		List<RadiusMarker> radiusMarkers = new ArrayList<>();

		for (ColourRadiusMarker cm : markers)
		{
			radiusMarkers.add(translateToRadiusMarker(cm));
		}

		saveMarkers(radiusMarkers);
	}

	public ColourRadiusMarker addMarker()
	{
		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());

		return findColourRadiusMarker(addMarker(worldPoint));
	}

	public RadiusMarker addMarker(WorldPoint worldPoint)
	{
		final RadiusMarker marker = new RadiusMarker(
			Instant.now().toEpochMilli(),
			DEFAULT_MARKER_NAME + " " + (markers.size() + 1),
			true,
			false,
			worldPoint.getPlane(),
			worldPoint.getX(),
			worldPoint.getY(),
			config.defaultColourSpawn(),
			true,
			config.defaultRadiusWander(),
			config.defaultColourWander(),
			true,
			config.defaultRadiusMax(),
			config.defaultColourMax(),
			true,
			config.defaultColourAggression(),
			true,
			config.defaultColourRetreatInteraction(),
			false,
			0,
			config.defaultRadiusAttack(),
			config.defaultColourAttack(),
			AttackType.MELEE,
			true,
			config.defaultRadiusHunt(),
			config.defaultColourHunt(),
			false,
			config.defaultRadiusInteraction(),
			config.defaultColourInteraction(),
			false);

		List<RadiusMarker> radiusMarkers = new ArrayList<>(getRadiusMarkers());
		if (!radiusMarkers.contains(marker))
		{
			radiusMarkers.add(marker);
		}

		saveMarkers(radiusMarkers);

		loadMarkers();

		pluginPanel.rebuild();

		return marker;
	}

	public void removeMarker(ColourRadiusMarker colourRadiusMarker)
	{
		RadiusMarker radiusMarker = translateToRadiusMarker(colourRadiusMarker);

		List<RadiusMarker> radiusMarkers = new ArrayList<>(getRadiusMarkers());

		radiusMarkers.remove(radiusMarker);

		saveMarkers(radiusMarkers);

		loadMarkers();

		pluginPanel.rebuild();
	}

	public Collection<WorldPoint> getInstanceWorldPoints(WorldPoint worldPointTemplate)
	{
		if (!client.isInInstancedRegion())
		{
			return Collections.singleton(worldPointTemplate);
		}

		return WorldPoint.toLocalInstance(client, worldPointTemplate);
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
				return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_DRAW_AREA);
			}
			return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_MINIMAP_DRAW_AREA);
		}
		return client.getWidget(ComponentID.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
	}
}
