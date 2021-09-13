package com.radiusmarkers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Radius Markers",
	description = "Highlight NPC radius regions like spawn, wander, retreat and aggro range",
	tags = {"radius", "region", "marker", "box", "square", "spawn", "wander", "retreat", "aggro", "range"}
)
public class RadiusMarkerPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "Radius Markers";
	private static final String CONFIG_GROUP = "radiusmarkers";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String REGION_PREFIX = "region_";
	private static final String DEFAULT_MARKER_NAME = "Marker";

	@Getter(AccessLevel.PACKAGE)
	private final List<ColourRadiusMarker> markers = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private RadiusMarkerConfig config;

	@Inject
	private RadiusMarkerOverlay overlay;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Gson gson;

	@Getter
	@Inject
	private ColorPickerManager colourPickerManager;

	private RadiusMarkerPluginPanel pluginPanel;
	private NavigationButton navigationButton;

	@Provides
	RadiusMarkerConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RadiusMarkerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		loadMarkers();

		pluginPanel = new RadiusMarkerPluginPanel(this, config);
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
		overlayManager.remove(overlay);
		markers.clear();
		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Map region has just been updated
		loadMarkers();
	}

	private void loadMarkers()
	{
		markers.clear();

		// final int[] regions = client.getMapRegions();
		final int[] regions = getAllRegions();

		if (regions == null)
		{
			return;
		}

		for (int regionId : regions)
		{
			final Collection<RadiusMarker> radiusMarkers = getMarkers(regionId);
			final Collection<ColourRadiusMarker> colourRadiusMarkers = translateToColourRadiusMarker(radiusMarkers, regionId);
			markers.addAll(colourRadiusMarkers);
		}
	}

	private int[] getAllRegions()
	{
		final String prefix = ConfigManager.getWholeKey(CONFIG_GROUP, null, REGION_PREFIX);

		List<String> keys = configManager.getConfigurationKeys(prefix);

		return keys.stream().map(k -> k.substring(prefix.length())).mapToInt(Integer::parseInt).toArray();
	}

	private Collection<RadiusMarker> getMarkers(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);

		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		return gson.fromJson(json, new TypeToken<List<RadiusMarker>>(){}.getType());
	}

	private void saveMarkers(int regionId, Collection<RadiusMarker> markers)
	{
		if (markers == null || markers.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
			return;
		}

		String json = gson.toJson(markers);
		configManager.setConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId, json);
	}

	private Collection<ColourRadiusMarker> translateToColourRadiusMarker(Collection<RadiusMarker> markers, int regionId)
	{
		if (markers.isEmpty())
		{
			return Collections.emptyList();
		}

		return markers.stream()
				.flatMap(marker ->
				{
					final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client,
							WorldPoint.fromRegion(regionId, marker.getSpawnRegionX(), marker.getSpawnRegionY(), marker.getZ()));
					return localWorldPoints.stream().map(worldPoint -> new ColourRadiusMarker(marker, worldPoint));
				})
				.collect(Collectors.toList());
	}

	private RadiusMarker translateToRadiusMarker(ColourRadiusMarker colourRadiusMarker)
	{
		return new RadiusMarker(
			colourRadiusMarker.getName(),
			colourRadiusMarker.isVisible(),
			colourRadiusMarker.getZ(),
			colourRadiusMarker.getWorldPoint().getRegionX(),
			colourRadiusMarker.getWorldPoint().getRegionY(),
			colourRadiusMarker.getSpawnColour(),
			colourRadiusMarker.isSpawnVisible(),
			colourRadiusMarker.getWanderRadius(),
			colourRadiusMarker.getWanderColour(),
			colourRadiusMarker.isWanderVisible(),
			colourRadiusMarker.getRetreatRadius(),
			colourRadiusMarker.getRetreatColour(),
			colourRadiusMarker.isRetreatVisible(),
			colourRadiusMarker.getAggroRadius(),
			colourRadiusMarker.getAggroColour(),
			colourRadiusMarker.isAggroVisible());
	}

	public void saveMarkers(int regionId)
	{
		List<RadiusMarker> radiusMarkers = new ArrayList<>();

		for (ColourRadiusMarker cm : markers)
		{
			if (cm.getWorldPoint().getRegionID() != regionId)
			{
				continue;
			}
			radiusMarkers.add(translateToRadiusMarker(cm));
		}

		saveMarkers(regionId, radiusMarkers);
	}

	public void addMarker()
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());

		addMarker(worldPoint);
	}

	public void addMarker(WorldPoint worldPoint)
	{
		final int regionId = worldPoint.getRegionID();

		final RadiusMarker marker = new RadiusMarker(
			DEFAULT_MARKER_NAME + " " + (markers.size() + 1),
			true,
			worldPoint.getPlane(),
			worldPoint.getRegionX(),
			worldPoint.getRegionY(),
			config.defaultColourSpawn(),
			true,
			config.defaultRadiusWander(),
			config.defaultColourWander(),
			true,
			config.defaultRadiusRetreat(),
			config.defaultColourRetreat(),
			true,
			config.defaultRadiusAggro(),
			config.defaultColourAggro(),
			true);

		List<RadiusMarker> radiusMarkers = new ArrayList<>(getMarkers(regionId));
		if (radiusMarkers.contains(marker))
		{
			radiusMarkers.remove(marker);
		}
		else
		{
			radiusMarkers.add(marker);
		}

		saveMarkers(regionId, radiusMarkers);

		loadMarkers();

		pluginPanel.rebuild();
	}

	public void removeMarker(ColourRadiusMarker colourRadiusMarker)
	{
		RadiusMarker radiusMarker = translateToRadiusMarker(colourRadiusMarker);

		final int regionId = colourRadiusMarker.getWorldPoint().getRegionID();

		List<RadiusMarker> radiusMarkers = new ArrayList<>(getMarkers(regionId));

		radiusMarkers.remove(radiusMarker);

		saveMarkers(regionId, radiusMarkers);

		loadMarkers();

		pluginPanel.rebuild();
	}
}
