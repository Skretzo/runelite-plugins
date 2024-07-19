package com.invalidmovement;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

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
		name = "Radius options",
		description = "Options for radius of displaying invalid movement",
		position = 4
	)
	String sectionRadius = "sectionRadius";

	@Range(
		min = -1
	)
	@ConfigItem(
		keyName = "radiusScene",
		name = "Scene radius",
		description = "The radius in number of tiles around the player in which the<br>"
			+ "invalid movement blocking tiles are shown in the game scene.<br>"
			+ "Use -1 to show everything and not restrict to a radius.",
		position = 5,
		section = sectionRadius
	)
	default int radiusScene()
	{
		return 25;
	}

	@Range(
		min = -1
	)
	@ConfigItem(
		keyName = "radiusMinimap",
		name = "Minimap radius",
		description = "The radius in number of tiles around the player in which the<br>"
			+ "invalid movement blocking tiles are shown on the minimap.<br>"
			+ "Use -1 to show everything and not restrict to a radius.",
		position = 6,
		section = sectionRadius
	)
	default int radiusMinimap()
	{
		return -1;
	}

	@Range(
		min = -1
	)
	@ConfigItem(
		keyName = "radiusWorldMap",
		name = "World map radius",
		description = "The radius in number of tiles around the player in which the<br>"
			+ "invalid movement blocking tiles are shown on the world map.<br>"
			+ "Use -1 to show everything and not restrict to a radius.",
		position = 7,
		section = sectionRadius
	)
	default int radiusWorldMap()
	{
		return -1;
	}

	@ConfigSection(
		name = "Colour options",
		description = "Options for colouring the different invalid movement tiles",
		position = 8
	)
	String sectionColours = "sectionColours";

	@Alpha
	@ConfigItem(
		keyName = "colourFloor",
		name = "Floor colour",
		description = "Colour for invalid movement floor tiles",
		position = 9,
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
		position = 10,
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
		position = 11,
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
		position = 12
	)
	default int wallWidth()
	{
		return 2;
	}
}
