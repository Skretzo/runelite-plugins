package com.polygonlimiter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("polygonlimiter")
public interface PolygonLimiterConfig extends Config
{
	/*
	@ConfigItem(
		keyName = "decorativeObjectLimit",
		name = "Decorative object limit",
		description = "The maximum number of decorative object model vertices.<br>Restart the plugin to apply changes"
	)
	default int decorativeObjectLimit()
	{
		return 1000;
	}
	*/

	@ConfigItem(
		keyName = "gameObjectLimit",
		name = "Game object limit",
		description = "The maximum number of game object model vertices.<br>Restart the plugin to apply changes"
	)
	default int gameObjectLimit()
	{
		return 1000;
	}

	@ConfigItem(
		keyName = "groundObjectLimit",
		name = "Ground object limit",
		description = "The maximum number of ground object model vertices.<br>Restart the plugin to apply changes"
	)
	default int groundObjectLimit()
	{
		return 1000;
	}

	/*
	@ConfigItem(
		keyName = "wallObjectLimit",
		name = "Wall object limit",
		description = "The maximum number of wall object model vertices.<br>Restart the plugin to apply changes"
	)
	default int wallObjectLimit()
	{
		return 1000;
	}
	*/

	@ConfigItem(
		keyName = "removeTiles",
		name = "Remove all tiles",
		description = "Whether to remove all tiles.<br>Restart the plugin to apply changes." +
			"<br>Use the skybox plugin to change the colour of the void for green screening."
	)
	default boolean removeTiles()
	{
		return false;
	}
}
