package com.noexamine;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;
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
	private static final String REMOVE = "Remove";
	private static final int POH_BUILDING_MODE_VARBIT = 2176;
	private static final Set<Integer> POH_REGION_IDS = new HashSet<>(Arrays.asList(7258, 7514, 7770, 8026, 7257, 7513, 7769, 8025));

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
			MenuAction menuAction = menuEntry.getType();

			if (!isExamine(menuAction, menuEntry.getOption()) &&
				!isCancel(menuAction) &&
				!isRemove(menuAction, menuEntry.getOption()) &&
				!isWalkHere(menuAction))
			{
				alteredMenuEntries.add(menuEntry);
			}
		}

		client.setMenuEntries(alteredMenuEntries.toArray(new MenuEntry[0]));
	}

	private boolean isExamine(MenuAction menuAction, String option)
	{
		if (client.isKeyPressed(KeyCode.KC_SHIFT) && config.examineShift())
		{
			return false;
		}
		return (MenuAction.EXAMINE_ITEM_GROUND.equals(menuAction) && config.examineItemsGround()) ||
				(MenuAction.EXAMINE_NPC.equals(menuAction) && config.examineNpcs()) ||
				(MenuAction.EXAMINE_OBJECT.equals(menuAction) && config.examineObjects()) ||
				(MenuAction.CC_OP_LOW_PRIORITY.equals(menuAction) && config.examineItemInventory() && EXAMINE.equals(option));
	}

	private boolean isPoh()
	{
		return client.getVarbitValue(POH_BUILDING_MODE_VARBIT) != 1 &&
			POH_REGION_IDS.contains(WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID());
	}

	private boolean isCancel(MenuAction menuAction)
	{
		return MenuAction.CANCEL.equals(menuAction) && config.cancelEverywhere();
	}

	private boolean isRemove(MenuAction menuAction, String option)
	{
		return isPoh() && MenuAction.GAME_OBJECT_FIFTH_OPTION.equals(menuAction) && REMOVE.equals(option) && config.removePoh();
	}

	private boolean isWalkHere(MenuAction menuAction)
	{
		return MenuAction.WALK.equals(menuAction) && config.walkHereEverywhere();
	}
}
