package com.noexamine;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

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

	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	private final Predicate<MenuEntry> filterMenuEntries = entry ->
	{
		MenuAction menuAction = MenuAction.of(entry.getType().getId());

		if (!shiftModifier())
		{
			return (!MenuAction.EXAMINE_ITEM_GROUND.equals(menuAction) || !config.itemsGround()) &&
				(!MenuAction.EXAMINE_NPC.equals(menuAction) || !config.npcs()) &&
				(!MenuAction.EXAMINE_OBJECT.equals(menuAction) || !config.objects()) &&
				(!MenuAction.CC_OP_LOW_PRIORITY.equals(menuAction) || !config.itemInventory()
					|| !EXAMINE.equals(entry.getOption()) || entry.getParam1() == WidgetInfo.BANK_ITEM_CONTAINER.getId());
		}

		return true;
	};


	private MenuEntry[] updateMenuEntries(MenuEntry[] menuEntries)
	{
		return Arrays.stream(menuEntries)
			.filter(filterMenuEntries).sorted((o1, o2) -> 0)
			.toArray(MenuEntry[]::new);
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen())
		{
			MenuEntry[] menuEntries = client.getMenuEntries();
			int idx = 0;
			optionIndexes.clear();
			for (MenuEntry entry : menuEntries)
			{
				String option = Text.removeTags(entry.getOption()).toLowerCase();
				optionIndexes.put(option, idx++);
			}
		}

		client.setMenuEntries(updateMenuEntries(client.getMenuEntries()));
	}

	private boolean shiftModifier()
	{
		return client.isKeyPressed(KeyCode.KC_SHIFT);
	}
}
