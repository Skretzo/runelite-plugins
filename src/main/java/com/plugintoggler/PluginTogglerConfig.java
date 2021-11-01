package com.plugintoggler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PluginTogglerPlugin.CONFIG_GROUP)
public interface PluginTogglerConfig extends Config
{
	@ConfigItem(
		keyName = "reportButton",
		name = "Report Button",
		description = "Whether to show report button toggling"
	)
	default boolean reportButton()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fishing",
		name = "Fishing",
		description = "Whether to show fishing toggling"
	)
	default boolean fishing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "combatLevel",
		name = "Combat Level",
		description = "Whether to show combat level toggling"
	)
	default boolean combatLevel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "woodcutting",
		name = "Woodcutting",
		description = "Whether to show woodcutting toggling"
	)
	default boolean woodcutting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "mining",
		name = "Mining",
		description = "Whether to show mining toggling"
	)
	default boolean mining()
	{
		return true;
	}
}
