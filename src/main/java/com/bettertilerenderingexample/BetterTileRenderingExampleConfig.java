package com.bettertilerenderingexample;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("bettertilerenderingexample")
public interface BetterTileRenderingExampleConfig extends Config
{
	@ConfigItem(
		keyName = "renderTiles",
		name = "Render tiles",
		description = "Render N x N tiles centered around the player one at a time",
		position = 0
	)
	default boolean renderTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "renderLines",
		name = "Render lines",
		description = "Render N x N tiles centered around the player as a semi-continuous line",
		position = 1
	)
	default boolean renderLines()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "colourTiles",
		name = "Tile colour",
		description = "Configures the colour of the tiles",
		position = 2
	)
	default Color colourTiles()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "colourLines",
		name = "Line colour",
		description = "Configures the colour of the lines",
		position = 3
	)
	default Color colourLines()
	{
		return Color.RED;
	}

	@Range(
		min = 1
	)
	@ConfigItem(
		keyName = "radius",
		name = "Radius",
		description = "Rendering radius for the tiles/lines",
		position = 4
	)
	default int radius()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Width of the tile/line border",
		position = 5
	)
	default double borderWidth()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "showInfo",
		name = "Show info",
		description = "Display info about rendering efficiency in an overlay",
		position = 6
	)
	default boolean showInfo()
	{
		return true;
	}
}
