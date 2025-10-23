package com.polygonlimiter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

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
		keyName = "keepInteractableObjects",
		name = "Keep interactable objects",
		description = "Whether to keep objects with interactions (e.g. 'Chop down Tree')<br>" +
			"regardless of the object model vertices limit.<br>" +
			"The 'Examine' option is not considered as an interaction.<br>" +
			"Restart the plugin to apply changes."
	)
	default boolean keepInteractableObjects()
	{
		return false;
	}

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

	@Range(
		min = -1
	)
	@ConfigItem(
		keyName = "removeTilesRadius",
		name = "Remove all tiles radius",
		description = "The radius in which to remove all tiles outside of.<br>Restart the plugin to apply changes." +
			"<br>Use the skybox plugin to change the colour of the void for green screening."
	)
	default int removeTilesRadius()
	{
		return -1;
	}
}
