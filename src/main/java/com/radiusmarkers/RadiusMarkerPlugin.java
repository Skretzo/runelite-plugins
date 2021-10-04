package com.radiusmarkers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
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
	description = "Highlight NPC radius regions like spawn, wander, retreat and max range",
	tags = {"radius", "region", "marker", "box", "square", "spawn", "wander", "retreat", "max", "aggro", "range"}
)
public class RadiusMarkerPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "Radius Markers";
	private static final String CONFIG_GROUP = "radiusmarkers";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String REGION_PREFIX = "region_";
	private static final String DEFAULT_MARKER_NAME = "Marker";
	private static final String RENAME_MARKER = "Rename marker";

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

		clientToolbar.addNavigation(navigationButton);
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

			MenuEntry[] menuEntries = client.getMenuEntries();

			menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

			final MenuEntry renameEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();
			renameEntry.setOption(RENAME_MARKER);
			renameEntry.setTarget(event.getTarget().split("  ")[0] + " " + npc.getId() + "#" + npc.getIndex());
			renameEntry.setParam0(event.getActionParam0());
			renameEntry.setParam1(event.getActionParam1());
			renameEntry.setIdentifier(event.getIdentifier());
			renameEntry.setType(MenuAction.RUNELITE.getId());

			client.setMenuEntries(menuEntries);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked click)
	{
		if (!MenuAction.RUNELITE.equals(click.getMenuAction()) || !click.getMenuOption().equals(RENAME_MARKER))
		{
			return;
		}

		final int id = click.getId();
		final NPC[] cachedNPCs = client.getCachedNPCs();
		final NPC npc = cachedNPCs[id];

		if (npc == null || npc.getName() == null || renameMarker == null)
		{
			return;
		}

		renameMarker.getPanel().setMarkerText(npc.getName() + " " + npc.getId() + "#" + npc.getIndex());

		click.consume();
	}

	private void loadMarkers()
	{
		markers.clear();

		final int[] regions = getAllRegions();

		if (regions == null)
		{
			return;
		}

		for (int regionId : regions)
		{
			final Collection<RadiusMarker> radiusMarkers = getMarkers(regionId);
			final List<ColourRadiusMarker> colourRadiusMarkers = translateToColourRadiusMarker(radiusMarkers, regionId);
			markers.addAll(colourRadiusMarkers);
		}

		Collections.sort(markers);
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

	private List<ColourRadiusMarker> translateToColourRadiusMarker(Collection<RadiusMarker> markers, int regionId)
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
			colourRadiusMarker.isCollapsed(),
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
			colourRadiusMarker.getMaxRadius(),
			colourRadiusMarker.getMaxColour(),
			colourRadiusMarker.isMaxVisible());
	}

	private ColourRadiusMarker findColourRadiusMarker(RadiusMarker radiusMarker)
	{
		for (final ColourRadiusMarker colourRadiusMarker : markers)
		{
			if (radiusMarker.getName().equals(colourRadiusMarker.getName()) &&
				radiusMarker.isVisible() == colourRadiusMarker.isVisible() &&
				radiusMarker.isCollapsed() == colourRadiusMarker.isCollapsed() &&
				radiusMarker.getZ() == colourRadiusMarker.getZ() &&
				radiusMarker.getSpawnRegionX() == colourRadiusMarker.getWorldPoint().getRegionX() &&
				radiusMarker.getSpawnRegionY() == colourRadiusMarker.getWorldPoint().getRegionY() &&
				radiusMarker.getSpawnColour().equals(colourRadiusMarker.getSpawnColour()) &&
				radiusMarker.isSpawnVisible() == colourRadiusMarker.isSpawnVisible() &&
				radiusMarker.getWanderRadius() == colourRadiusMarker.getWanderRadius() &&
				radiusMarker.getWanderColour().equals(colourRadiusMarker.getWanderColour()) &&
				radiusMarker.isWanderVisible() == colourRadiusMarker.isWanderVisible() &&
				radiusMarker.getRetreatRadius() == colourRadiusMarker.getRetreatRadius() &&
				radiusMarker.getRetreatColour().equals(colourRadiusMarker.getRetreatColour()) &&
				radiusMarker.isRetreatVisible() == colourRadiusMarker.isRetreatVisible() &&
				radiusMarker.getMaxRadius() == colourRadiusMarker.getMaxRadius() &&
				radiusMarker.getMaxColour().equals(colourRadiusMarker.getMaxColour()) &&
				radiusMarker.isMaxVisible() == colourRadiusMarker.isMaxVisible())
			{
				return colourRadiusMarker;
			}
		}
		return null;
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
		final int regionId = worldPoint.getRegionID();

		final RadiusMarker marker = new RadiusMarker(
			DEFAULT_MARKER_NAME + " " + (markers.size() + 1),
			true,
			false,
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
			config.defaultRadiusMax(),
			config.defaultColourMax(),
			true);

		List<RadiusMarker> radiusMarkers = new ArrayList<>(getMarkers(regionId));
		if (!radiusMarkers.contains(marker))
		{
			radiusMarkers.add(marker);
		}

		saveMarkers(regionId, radiusMarkers);

		loadMarkers();

		pluginPanel.rebuild();

		return marker;
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
