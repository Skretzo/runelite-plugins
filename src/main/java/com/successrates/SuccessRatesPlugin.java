package com.successrates;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Success Rates",
	description = "Track and display skill level dependant action success rates",
	tags = {"skilling", "action", "lvl", "success", "rate", "tracking", "distribution", "probability"}
)
public class SuccessRatesPlugin extends Plugin
{
	private NavigationButton navigationButton;
	private SuccessRatesPluginPanel pluginPanel;

	@Inject
	private Gson gson;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SuccessRatesConfig config;

	@Provides
	SuccessRatesConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SuccessRatesConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		pluginPanel = new SuccessRatesPluginPanel(config, this, client, configManager, gson, eventBus);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "success_rates_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Success Rates")
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		// Do we really have to save the tracker data every time we perform an action in-game?
		// Maybe only save here on shutDown? What about X-ing the client?

		for (SuccessRatesSkill skill : pluginPanel.getTrackers().keySet())
		{
			for (SuccessRatesTracker tracker : pluginPanel.getTrackers().get(skill))
			{
				tracker.unregister();
			}
		}

		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;
	}

	public void updatePanel()
	{
		pluginPanel.repaint();
		pluginPanel.revalidate();
	}

	public void rebuildPanel()
	{
		pluginPanel.displaySelectedTracker();
	}
}
