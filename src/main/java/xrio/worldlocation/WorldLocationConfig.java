/*
 * Copyright (c) 2020, Xrio
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package xrio.worldlocation;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("worldlocation")
public interface WorldLocationConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "tileLocation",
		name = "Tile Location",
		description = "Show world tile (1 x 1) location"
	)
	default boolean tileLocation()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "chunkLines",
		name = "Chunk Lines",
		description = "Show chunk (8 x 8) lines"
	)
	default boolean chunkLines()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "regionLines",
		name = "Region Lines",
		description = "Show region (64 x 64) lines"
	)
	default boolean regionLines()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "mapTileLines",
		name = "World Map Tile Lines",
		description = "Show tile (1 x 1) lines on the world map"
	)
	default boolean mapTileLines()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "mapChunkLines",
		name = "World Map Chunk Lines",
		description = "Show chunk (8 x 8) lines on the world map"
	)
	default boolean mapChunkLines()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "mapRegionLines",
		name = "World Map Region Lines",
		description = "Show region (64 x 64) lines on the world map"
	)
	default boolean mapRegionLines()
	{
		return false;
	}

	@ConfigSection(
		position = 6,
		name = "Settings",
		description = "Colour and line width options",
		closedByDefault = true
	)
	String settingsSection = "settingsSection";

	@Range(
		max = 255
	)
	@ConfigItem(
		position = 7,
		keyName = "tileAlpha",
		name = "Tile Colour Alpha",
		description = "The alpha value [0, 255] for the world point tile colour",
		section = settingsSection
	)
	default int tileAlpha()
	{
		return 127;
	}

	@ConfigItem(
		position = 8,
		keyName = "tileColour",
		name = "Tile Colour",
		description = "The colour of the tile for the world point location",
		section = settingsSection
	)
	default Color tileColour()
	{
		return new Color(0, 0, 0, 127);
	}

	@ConfigItem(
		position = 9,
		keyName = "chunkLineColour",
		name = "Chunk Line Colour",
		description = "The colour of the chunk border",
		section = settingsSection
	)
	default Color chunkLineColour()
	{
		return Color.BLUE;
	}

	@ConfigItem(
		position = 10,
		keyName = "regionLineColour",
		name = "Region Line Colour",
		description = "The colour of the region border",
		section = settingsSection
	)
	default Color regionLineColour()
	{
		return Color.GREEN;
	}

	@Range(
		max = 5
	)
	@ConfigItem(
		position = 11,
		keyName = "chunkLineWidth",
		name = "Chunk Line Width",
		description = "The chunk border line width",
		section = settingsSection
	)
	default int chunkLineWidth()
	{
		return 2;
	}

	@Range(
		max = 5
	)
	@ConfigItem(
		position = 12,
		keyName = "regionLineWidth",
		name = "Region Line Width",
		description = "The region border line width",
		section = settingsSection
	)
	default int regionLineWidth()
	{
		return 4;
	}
}
