package com.invalidmovement;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("invalidmovement")
public interface InvalidMovementConfig extends Config
{
	@ConfigItem(
		keyName = "showScene",
		name = "Show in scene",
		description = "Show the invalid movement blocking tiles in the game scene",
		position = 0
	)
	default boolean showScene()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showMinimap",
		name = "Show on minimap",
		description = "Show the invalid movement blocking tiles on the minimap",
		position = 1
	)
	default boolean showMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "Show on world map",
		description = "Show the invalid movement blocking tiles on the world map",
		position = 2
	)
	default boolean showWorldMap()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "colour",
		name = "Colour",
		description = "Colour for invalid movement blocking tiles",
		position = 3
	)
	default Color colour()
	{
		return new Color(255, 0, 0, 127);
	}

	@ConfigItem(
		keyName = "wallWidth",
		name = "Wall width",
		description = "Invalid movement blocking wall width",
		position = 4
	)
	default int wallWidth()
	{
		return 2;
	}
}
