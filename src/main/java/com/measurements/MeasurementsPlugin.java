package com.measurements;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Measurements",
	description = "Measure distance to nearby NPCs by shift-clicking them. The result is displayed in a panel or copied to your clipboard.",
	tags = {"measure", "distance", "tiles"}
)
public class MeasurementsPlugin extends Plugin
{
	private static final String MEASURE_DISTANCE_OPTION = "Distance to";

	private MeasurementsPanel pluginPanel;
	private NavigationButton navigationButton;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private MeasurementsConfig config;

	@Provides
	MeasurementsConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MeasurementsConfig.class);
	}

	@Override
	protected void startUp()
	{
		pluginPanel = new MeasurementsPanel();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Measurements")
			.icon(icon)
			.priority(100)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navigationButton);
		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		MenuEntry[] newEntries = new MenuEntry[1];

		if (event.getType() == MenuAction.EXAMINE_NPC.getId())
		{
			newEntries = new MenuEntry[2];
			newEntries[1] = client.createMenuEntry(-1)
				.setType(MenuAction.RUNELITE)
				.setIdentifier(event.getIdentifier())
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setOption(MEASURE_DISTANCE_OPTION)
				.setTarget(event.getTarget())
				.onClick(this::measureDistance);
		}

		for (MenuEntry entry : client.getMenuEntries())
		{
			if (MenuAction.CANCEL.equals(entry.getType()))
			{
				newEntries[0] = entry;
				break;
			}
		}

		client.setMenuEntries(newEntries);
	}

	private void measureDistance(MenuEntry entry)
	{
		NPC npc = client.getCachedNPCs()[entry.getIdentifier()];
		Player player = client.getLocalPlayer();
		if (npc == null || player == null)
		{
			return;
		}

		String measurement = config.outputFormat()
			.replace("{ID}", Integer.toString(npc.getId()))
			.replace("{index}", Integer.toString(npc.getIndex()))
			.replace("{distance}", Integer.toString(distanceBetween(player.getWorldLocation(), npc.getWorldLocation())));

		pluginPanel.appendText(measurement);
		if (config.copyToClipboard())
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(measurement), null);
		}
	}

	private int distanceBetween(WorldPoint start, WorldPoint end)
	{
		int diagonal = config.diagonalLength();

		int dx = Math.abs(start.getX() - end.getX());
		int dy = Math.abs(start.getY() - end.getY());

		if (diagonal == 2)
		{
			return dx + dy;
		}

		return Math.max(dx, dy);
	}
}
