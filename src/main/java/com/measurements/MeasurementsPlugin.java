package com.measurements;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Measurements",
	description = "Measure distance to nearby NPCs by shift-clicking them. The result is displayed in a panel or copied to your clipboard.",
	tags = {"measure", "distance", "tiles"}
)
public class MeasurementsPlugin extends Plugin
{
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
		pluginPanel = new MeasurementsPanel(config);

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
}
