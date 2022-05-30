package com.plugintoggler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.config.ConfigPlugin;
import net.runelite.client.plugins.config.ConfigService;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Plugin Toggler",
	description = "Find and toggle plugins in-game in appropriate right-click menus",
	tags = {"plugin", "toggle", "enable", "disable"}
)
@PluginDependency(ConfigPlugin.class)
public class PluginTogglerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "plugintoggler";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ConfigService configService;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private PluginTogglerConfig config;

	@Provides
	PluginTogglerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PluginTogglerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		updateConfigStates();
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		updateConfigStates();
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		int type = event.getType();
		if (type == MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;
		}

		final MenuAction menuAction = MenuAction.of(type);
		final String menuFindOption = event.getOption();
		final String menuFindTarget = event.getTarget();

		for (final PluginTogglerMenu pluginTogglerMenu : PluginTogglerMenu.values())
		{
			if (pluginTogglerMenu.equals(menuAction, client, menuFindOption, menuFindTarget))
			{
				addMenuEntry(event, pluginTogglerMenu.getMenuDisplayTarget());
				break;
			}
		}
	}

	@Subscribe
	public void onMenuShouldLeftClick(final MenuShouldLeftClick event)
	{
		if (!config.forceRightClick())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		PluginTogglerMenu[] pluginTogglerMenus = PluginTogglerMenu.values();
		for (MenuEntry menuEntry : menuEntries)
		{
			final String menuDisplayOption = menuEntry.getOption();
			final String menuDisplayTarget = menuEntry.getTarget();
			for (PluginTogglerMenu pluginTogglerMenu : pluginTogglerMenus)
			{
				if (pluginTogglerMenu.displayEquals(menuDisplayOption, menuDisplayTarget))
				{
					event.setForceRightClick(true);
					return;
				}
			}
		}
	}

	private void onMenuEntryClicked(final MenuEntry menuEntry)
	{
		if (!MenuAction.RUNELITE.equals(menuEntry.getType()))
		{
			return;
		}

		final String menuDisplayOption = menuEntry.getOption();
		final String menuDisplayTarget = menuEntry.getTarget();

		for (final PluginTogglerMenu pluginTogglerMenu : PluginTogglerMenu.values())
		{
			if (pluginTogglerMenu.displayEquals(menuDisplayOption, menuDisplayTarget))
			{
				togglePlugin(pluginTogglerMenu.getPluginName());
				break;
			}
		}
	}

	private void addMenuEntry(final MenuEntryAdded event, final String target)
	{
		if (config.requireShift() && !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		final String option = PluginTogglerMenu.MENU_DISPLAY_OPTION;
		for (MenuEntry menuEntry : client.getMenuEntries())
		{
			if (option.equals(menuEntry.getOption()) && target.equals(menuEntry.getTarget()))
			{
				return;
			}
		}

		client.createMenuEntry(-1)
			.setOption(PluginTogglerMenu.MENU_DISPLAY_OPTION)
			.setTarget(target)
			.setParam0(event.getActionParam0())
			.setParam1(event.getActionParam1())
			.setIdentifier(event.getIdentifier())
			.setType(MenuAction.RUNELITE)
			.onClick(this::onMenuEntryClicked);
	}

	private void togglePlugin(final String name)
	{
		Plugin plugin = null;
		for (final Plugin p : pluginManager.getPlugins())
		{
			if (name.equals(p.getName()))
			{
				plugin = p;
				break;
			}
		}
		if (plugin != null)
		{
			Plugin finalPlugin = plugin;
			SwingUtilities.invokeLater(() ->
			{
				try
				{
					final boolean enabled = pluginManager.isPluginEnabled(finalPlugin);
					pluginManager.setPluginEnabled(finalPlugin, !enabled);
					if (enabled)
					{
						pluginManager.stopPlugin(finalPlugin);
					}
					else
					{
						pluginManager.startPlugin(finalPlugin);
					}
				}
				catch (PluginInstantiationException e)
				{
					log.warn("Error when toggling plugin {}", finalPlugin.getClass().getSimpleName(), e);
				}
			});
			if (config.openConfig())
			{
				configService.openConfig(finalPlugin.getName());
			}
		}
	}

	private void updateConfigStates()
	{
		final String prefix = ConfigManager.getWholeKey(CONFIG_GROUP, null, "");
		final List<String> keys = configManager.getConfigurationKeys(prefix);

		final Map<String, Boolean> configStates = new HashMap<>();
		for (final String key : keys)
		{
			final String suffix = key.substring(CONFIG_GROUP.length() + 1);
			final String json = configManager.getConfiguration(CONFIG_GROUP, suffix);
			configStates.put(suffix, gson.fromJson(json, new TypeToken<Boolean>(){}.getType()));
		}

		for (final PluginTogglerMenu pluginTogglerMenu : PluginTogglerMenu.values())
		{
			final String keyName = pluginTogglerMenu.getConfigName();
			if (configStates.containsKey(keyName))
			{
				pluginTogglerMenu.setConfigEnabled(configStates.get(keyName));
			}
		}
	}
}
