package com.plugintoggler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.text.CaseUtils;

@Slf4j
@PluginDescriptor(
	name = "Plugin Toggler",
	description = "Adds the ability to toggle plugins in-game in appropriate right-click menus",
	tags = {"plugin", "toggle", "enable", "disable"}
)
public class PluginTogglerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "plugintoggler";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private CombatLevelOverlay combatLevelOverlay;

	@Provides
	PluginTogglerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PluginTogglerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(combatLevelOverlay);
		updateConfigStates();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(combatLevelOverlay);
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
			if (pluginTogglerMenu.equals(menuAction, menuFindOption, menuFindTarget))
			{
				addMenuEntry(event, pluginTogglerMenu.getMenuDisplayTarget());
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked click)
	{
		if (!MenuAction.RUNELITE.equals(click.getMenuAction()) &&
			!MenuAction.RUNELITE_OVERLAY.equals(click.getMenuAction()))
		{
			return;
		}

		final String menuShowTarget = click.getMenuTarget();

		for (final PluginTogglerMenu pluginTogglerMenu : PluginTogglerMenu.values())
		{
			if (pluginTogglerMenu.displayEquals(menuShowTarget))
			{
				togglePlugin(pluginTogglerMenu.getPluginName());
				break;
			}
		}

		click.consume();
	}

	private void addMenuEntry(final MenuEntryAdded event, final String target)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);

		final MenuEntry newEntry = menuEntries[menuEntries.length - 1] = new MenuEntry();
		newEntry.setOption(PluginTogglerMenu.MENU_DISPLAY_OPTION);
		newEntry.setTarget(target);
		newEntry.setParam0(event.getActionParam0());
		newEntry.setParam1(event.getActionParam1());
		newEntry.setIdentifier(event.getIdentifier());
		newEntry.setType(MenuAction.RUNELITE.getId());

		client.setMenuEntries(menuEntries);
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
						// Add finalPlugin.getName() to the plugin panel search bar ?
					}
				}
				catch (PluginInstantiationException e)
				{
					log.warn("Error when toggling plugin {}", finalPlugin.getClass().getSimpleName(), e);
				}
			});
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
			final String keyName = CaseUtils.toCamelCase(pluginTogglerMenu.getPluginName(), false, ' ');
			if (configStates.containsKey(keyName))
			{
				pluginTogglerMenu.setConfigEnabled(configStates.get(keyName));
			}
		}
	}
}
