package com.radiusmarkers;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
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
		final List<ColourRadiusMarker> colourRadiusMarkers = translateToColourRadiusMarker(radiusMarkers);
		markers.addAll(colourRadiusMarkers);

		Collections.sort(markers);
	}

	private Collection<RadiusMarker> getRadiusMarkers()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);

		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		return gson.fromJson(json, new TypeToken<List<RadiusMarker>>(){}.getType());
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
			colourRadiusMarker.getInteractionOrigin(),
			colourRadiusMarker.isInteractionVisible());
	}

	private ColourRadiusMarker findColourRadiusMarker(RadiusMarker radiusMarker)
	{
		for (final ColourRadiusMarker colourRadiusMarker : markers)
		{
			if (radiusMarker.getId() == colourRadiusMarker.getId() &&
				radiusMarker.getName().equals(colourRadiusMarker.getName()) &&
				radiusMarker.isVisible() == colourRadiusMarker.isVisible() &&
				radiusMarker.isCollapsed() == colourRadiusMarker.isCollapsed() &&
				radiusMarker.getZ() == colourRadiusMarker.getZ() &&
				radiusMarker.getSpawnX() == colourRadiusMarker.getSpawnX() &&
				radiusMarker.getSpawnY() == colourRadiusMarker.getSpawnY() &&
				radiusMarker.getSpawnColour().equals(colourRadiusMarker.getSpawnColour()) &&
				radiusMarker.isSpawnVisible() == colourRadiusMarker.isSpawnVisible() &&
				radiusMarker.getWanderRadius() == colourRadiusMarker.getWanderRadius() &&
				radiusMarker.getWanderColour().equals(colourRadiusMarker.getWanderColour()) &&
				radiusMarker.isWanderVisible() == colourRadiusMarker.isWanderVisible() &&
				radiusMarker.getMaxRadius() == colourRadiusMarker.getMaxRadius() &&
				radiusMarker.getMaxColour().equals(colourRadiusMarker.getMaxColour()) &&
				radiusMarker.isMaxVisible() == colourRadiusMarker.isMaxVisible() &&
				radiusMarker.getAggressionColour().equals(colourRadiusMarker.getAggressionColour()) &&
				radiusMarker.isAggressionVisible() == colourRadiusMarker.isAggressionVisible() &&
				radiusMarker.getRetreatInteractionColour().equals(colourRadiusMarker.getRetreatInteractionColour()) &&
				radiusMarker.isRetreatInteractionVisible() == colourRadiusMarker.isRetreatInteractionVisible() &&
				radiusMarker.getNpcId() == colourRadiusMarker.getNpcId() &&
				radiusMarker.getAttackRadius() == colourRadiusMarker.getAttackRadius() &&
				radiusMarker.getAttackColour().equals(colourRadiusMarker.getAttackColour()) &&
				radiusMarker.getAttackType().equals(colourRadiusMarker.getAttackType()) &&
				radiusMarker.isAttackVisible() == colourRadiusMarker.isAttackVisible() &&
				radiusMarker.getHuntRadius() == colourRadiusMarker.getHuntRadius() &&
				radiusMarker.getHuntColour().equals(colourRadiusMarker.getHuntColour()) &&
				radiusMarker.isHuntVisible() == colourRadiusMarker.isHuntVisible() &&
				radiusMarker.getInteractionRadius() == colourRadiusMarker.getInteractionRadius() &&
				radiusMarker.getInteractionColour().equals(colourRadiusMarker.getInteractionColour()) &&
				radiusMarker.getInteractionOrigin().equals(colourRadiusMarker.getInteractionOrigin()) &&
				radiusMarker.isInteractionVisible() == colourRadiusMarker.isInteractionVisible())
			{
				return colourRadiusMarker;
			}
		}
		return null;
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
			true,
			0,
			config.defaultRadiusAttack(),
			config.defaultColourAttack(),
			AttackType.MELEE,
			true,
			config.defaultRadiusHunt(),
			config.defaultColourHunt(),
			true,
			config.defaultRadiusInteraction(),
			config.defaultColourInteraction(),
			RadiusOrigin.DYNAMIC,
			true);

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
}
