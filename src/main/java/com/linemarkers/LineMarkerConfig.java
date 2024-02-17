package com.linemarkers;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(LineMarkerPlugin.CONFIG_GROUP)
public interface LineMarkerConfig extends Config
{
	@ConfigSection(
		name = "Default values",
		description = "Customize the marker range parameters to be displayed in the panel",
		position = 0
	)
	String defaultValues = "defaultValues";

	@Alpha
	@ConfigItem(
		keyName = "defaultColour",
		name = "Colour",
		description = "The default initial line colour when making a new line",
		position = 1,
		section = defaultValues
	)
	default Color defaultColour()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "defaultEdge",
		name = "Edge",
		description = "The default initial line edge when making a new line",
		position = 2,
		section = defaultValues
	)
	default Edge defaultEdge()
	{
		return Edge.WEST;
	}

	@Range(
		max = 10
	)
	@ConfigItem(
		keyName = "defaultWidth",
		name = "Width",
		description = "The default initial line width when making a new line",
		position = 3,
		section = defaultValues
	)
	default double defaultWidth()
	{
		return 3.0;
	}

	@ConfigItem(
		keyName = "showMinimap",
		name = "Show on minimap",
		description = "Display line markers on the minimap",
		position = 4
	)
	default boolean showMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "Show on world map",
		description = "Display line markers on the world map",
		position = 5
	)
	default boolean showWorldMap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideNavButton",
		name = "Hide side panel button",
		description = "Whether to hide the side panel button to reduce clutter when not needing to modify markers",
		position = 6
	)
	default boolean hideNavButton()
	{
		return false;
	}
}
