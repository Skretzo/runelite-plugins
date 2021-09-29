package com.invalidmovement;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("invalidmovement")
public interface InvalidMovementConfig extends Config
{
	@ConfigSection(
		name = "Display options",
		description = "Options for displaying invalid movement",
		position = 0
	)
	String sectionDisplay = "sectionDisplay";

	@ConfigItem(
		keyName = "showScene",
		name = "Show in scene",
		description = "Show the invalid movement blocking tiles in the game scene",
		position = 1,
		section = sectionDisplay
	)
	default boolean showScene()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showMinimap",
		name = "Show on minimap",
		description = "Show the invalid movement blocking tiles on the minimap",
		position = 2,
		section = sectionDisplay
	)
	default boolean showMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "Show on world map",
		description = "Show the invalid movement blocking tiles on the world map",
		position = 3,
		section = sectionDisplay
	)
	default boolean showWorldMap()
	{
		return true;
	}

	@ConfigSection(
		name = "Colour options",
		description = "Options for colouring the different invalid movement tiles",
		position = 4
	)
	String sectionColours = "sectionColours";

	@Alpha
	@ConfigItem(
		keyName = "colourFloor",
		name = "Floor colour",
		description = "Colour for invalid movement floor tiles",
		position = 5,
		section = sectionColours
	)
	default Color colourFloor()
	{
		return new Color(255, 0, 255, 127);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourObject",
		name = "Object colour",
		description = "Colour for invalid movement object tiles",
		position = 6,
		section = sectionColours
	)
	default Color colourObject()
	{
		return new Color(255, 0, 0, 127);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourWall",
		name = "Wall colour",
		description = "Colour for invalid movement wall tiles",
		position = 7,
		section = sectionColours
	)
	default Color colourWall()
	{
		return new Color(255, 255, 0, 127);
	}

	@ConfigItem(
		keyName = "wallWidth",
		name = "Wall width",
		description = "Invalid movement blocking wall width",
		position = 8
	)
	default int wallWidth()
	{
		return 2;
	}
}
