package com.snakemanmode;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(SnakemanModePlugin.CONFIG_GROUP)
public interface SnakemanModeConfig extends Config
{
	@ConfigSection(
		name = "Gameplay settings",
		description = "Settings for the gameplay",
		position = 0
	)
	String sectionGameplay = "sectionGameplay";

	@Range(
		min = 1000
	)
	@ConfigItem(
		keyName = "unlockXp",
		name = "Unlock xp",
		description = "The amount of experience required to unlock a new chunk",
		section = sectionGameplay,
		position = 1
	)
	default int unlockXp()
	{
		return 1000;
	}

	@ConfigSection(
		name = "Display settings",
		description = "Settings for the visual elements",
		position = 2
	)
	String sectionDisplay = "sectionDisplay";

	@ConfigItem(
		keyName = "showInfo",
		name = "Show info box",
		description = "Whether to display info about number of unlocked chunks and xp until next unlock in an overlay",
		section = sectionDisplay,
		position = 3
	)
	default boolean showInfo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFruitMapIndicator",
		name = "Show fruit map indicator",
		description = "Whether to display a focus point for the fruit chunk on the world map",
		section = sectionDisplay,
		position = 4
	)
	default boolean showFruitIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showChunkNumber",
		name = "Show chunk number",
		description = "Whether to display a number indicating the order in which the chunk was unlocked on the world map",
		section = sectionDisplay,
		position = 5
	)
	default boolean showChunkNumber()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawOutlineOnly",
		name = "Draw only outline",
		description = "Whether to only draw the combined chunk border outline and not individual chunk borders",
		section = sectionDisplay,
		position = 6
	)
	default boolean drawOutlineOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawLockedArea",
		name = "Draw locked area",
		description = "Whether to fill all locked chunks in the game scene with the specified fill colour",
		section = sectionDisplay,
		position = 7
	)
	default boolean drawLockedArea()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chunkBorderWidth",
		name = "Chunk border width",
		description = "The width of the chunk border in the game scene",
		section = sectionDisplay,
		position = 8
	)
	default double chunkBorderWidth()
	{
		return 2;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedBorderColour",
		name = "Unlocked border",
		description = "The colour of the unlocked chunk borders",
		section = sectionDisplay,
		position = 9
	)
	default Color unlockedBorderColour()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedFillColour",
		name = "Unlocked fill",
		description = "The fill colour of the unlocked chunks",
		section = sectionDisplay,
		position = 10
	)
	default Color unlockedFillColour()
	{
		return new Color(0, 255, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "lockedFillColour",
		name = "Locked fill",
		description = "The fill colour of the locked chunks",
		section = sectionDisplay,
		position = 11
	)
	default Color lockedFillColour()
	{
		return new Color(20, 40, 80, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkBorderColour",
		name = "Fruit chunk border",
		description = "The colour of the snake fruit chunk borders",
		section = sectionDisplay,
		position = 12
	)
	default Color fruitChunkBorderColour()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkFillColour",
		name = "Fruit chunk fill",
		description = "The fill colour of the snake fruit chunks",
		section = sectionDisplay,
		position = 13
	)
	default Color fruitChunkFillColour()
	{
		return new Color(255, 0, 255, 64);
	}

	@ConfigItem(
		keyName = "lastUnlockXp",
		name = "Last unlock xp",
		description = "The number of overall experience at the time of the last unlock",
		position = 14,
		hidden = true
	)
	default long lastUnlockXp()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "lastUnlockXp",
		name = "Set last unlock xp",
		description = "",
		position = 15
	)
	void lastUnlockXp(long xp);
}
