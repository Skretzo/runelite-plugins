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
		description = "The maximum number of decorative object model vertices"
	)
	default int decorativeObjectLimit()
	{
		return 50;
	}
	*/

	@ConfigItem(
		keyName = "gameObjectLimit",
		name = "Game object limit",
		description = "The maximum number of game object model vertices"
	)
	default int gameObjectLimit()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "groundObjectLimit",
		name = "Ground object limit",
		description = "The maximum number of ground object model vertices"
	)
	default int groundObjectLimit()
	{
		return 50;
	}

	/*
	@ConfigItem(
		keyName = "wallObjectLimit",
		name = "Wall object limit",
		description = "The maximum number of wall object model vertices"
	)
	default int wallObjectLimit()
	{
		return 50;
	}
	*/

	@ConfigItem(
		keyName = "removeTiles",
		name = "Remove all tiles",
		description = "Whether to remove all tiles.<br>Use the skybox plugin to change the colour of the void."
	)
	default boolean removeTiles()
	{
		return false;
	}
}
