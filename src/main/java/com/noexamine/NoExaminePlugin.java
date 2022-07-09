package com.noexamine;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "No Examine",
	description = "Remove examine menu options to never missclick again",
	tags = {"examine", "missclick"}
)
public class NoExaminePlugin extends Plugin
{
	private static final String EXAMINE = "Examine";

	@Inject
	private Client client;

	@Inject
	private NoExamineConfig config;

	@Provides
	NoExamineConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoExamineConfig.class);
	}

	@Subscribe(
		priority = -1
	)
	public void onMenuOpened(MenuOpened event)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		List<MenuEntry> alteredMenuEntries = new ArrayList<>();

		for (MenuEntry menuEntry : menuEntries)
		{
			MenuAction menuAction = MenuAction.of(menuEntry.getType().getId());

			if ((!MenuAction.EXAMINE_ITEM_GROUND.equals(menuAction) || !config.itemsGround()) &&
				(!MenuAction.EXAMINE_NPC.equals(menuAction) || !config.npcs()) &&
				(!MenuAction.EXAMINE_OBJECT.equals(menuAction) || !config.objects()) &&
				(!MenuAction.CC_OP_LOW_PRIORITY.equals(menuAction) || !config.itemInventory() || !EXAMINE.equals(menuEntry.getOption())))
			{
				alteredMenuEntries.add(menuEntry);
			}
		}

		client.setMenuEntries(alteredMenuEntries.toArray(new MenuEntry[0]));
	}
}
