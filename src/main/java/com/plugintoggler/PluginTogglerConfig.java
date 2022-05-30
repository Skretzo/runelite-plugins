package com.plugintoggler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(PluginTogglerPlugin.CONFIG_GROUP)
public interface PluginTogglerConfig extends Config
{
	@ConfigSection(
		name = "Settings",
		description = "Preference options for plugin toggling",
		position = 0
	)
	String settingsSection = "settingsSection";

	@ConfigItem(
		keyName = "forceRightClick",
		name = "Force right-click",
		description = "Whether to force the plugin toggle option to right-click if it is the topmost menu option",
		section = settingsSection
	)
	default boolean forceRightClick()
	{
		return true;
	}

	@ConfigItem(
		keyName = "openConfig",
		name = "Open config",
		description = "Whether to open the config panel when toggling a plugin",
		section = settingsSection
	)
	default boolean openConfig()
	{
		return true;
	}

	@ConfigItem(
		keyName = "requireShift",
		name = "Require shift",
		description = "Whether to require the shift key to be pressed to display the plugin toggle option in right-click menus",
		section = settingsSection
	)
	default boolean requireShift()
	{
		return false;
	}

	@ConfigSection(
		name = "Toggles",
		description = "List of plugins that can be toggled",
		position = 1
	)
	String toggleSection = "toggleSection";

	@ConfigItem(
		keyName = "combatLevel",
		name = "Combat Level",
		description = "Whether to show combat level toggling",
		section = toggleSection
	)
	default boolean combatLevel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fishing",
		name = "Fishing",
		description = "Whether to show fishing toggling",
		section = toggleSection
	)
	default boolean fishing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mining",
		name = "Mining",
		description = "Whether to show mining toggling",
		section = toggleSection
	)
	default boolean mining()
	{
		return true;
	}

	@ConfigItem(
		keyName = "reportButton",
		name = "Report Button",
		description = "Whether to show report button toggling",
		section = toggleSection
	)
	default boolean reportButton()
	{
		return true;
	}

	@ConfigItem(
		keyName = "woodcutting",
		name = "Woodcutting",
		description = "Whether to show woodcutting toggling",
		section = toggleSection
	)
	default boolean woodcutting()
	{
		return true;
	}
}
