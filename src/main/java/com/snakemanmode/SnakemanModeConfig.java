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

	@ConfigSection(
		name = "Skill settings",
		description = "Settings for the permitted skills to gain XP in",
		position = 1,
		closedByDefault = true
	)
	String sectionSkills = "sectionSkills";

	@ConfigSection(
		name = "Display settings",
		description = "Settings for the visual elements",
		position = 2
	)
	String sectionDisplay = "sectionDisplay";

	@ConfigSection(
		name = "Scene settings",
		description = "Settings for colouring the visual elements in the game scene",
		position = 3,
		closedByDefault = true
	)
	String sectionScene = "sectionScene";

	@ConfigSection(
		name = "Minimap settings",
		description = "Settings for colouring the visual elements on the minimap",
		position = 4,
		closedByDefault = true
	)
	String sectionMinimap = "sectionMinimap";

	@ConfigSection(
		name = "World map settings",
		description = "Settings for colouring the visual elements on the world map",
		position = 5,
		closedByDefault = true
	)
	String sectionWorldmap = "sectionWorldmap";

	@Range(
		min = 1000
	)
	@ConfigItem(
		keyName = "unlockXp",
		name = "Unlock xp",
		description = "The amount of experience required to unlock a new chunk",
		section = sectionGameplay,
		position = 6
	)
	default int unlockXp()
	{
		return 1000;
	}

	@ConfigItem(
		keyName = "onlyFreeToPlay",
		name = "Free-to-play only",
		description = "Whether to restrict fruit chunk generation to the free-to-play part of the world map",
		section = sectionGameplay,
		position = 7
	)
	default boolean onlyFreeToPlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeAgility",
		name = "Exclude Agility XP",
		description = "Whether to disallow XP gained in the Agility skill towards a new chunk unlock",
		section = sectionSkills,
		position = 8
	)
	default boolean excludeAgility()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeAttack",
		name = "Exclude Attack XP",
		description = "Whether to disallow XP gained in the Attack skill towards a new chunk unlock",
		section = sectionSkills,
		position = 9
	)
	default boolean excludeAttack()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeConstruction",
		name = "Exclude Construction XP",
		description = "Whether to disallow XP gained in the Construction skill towards a new chunk unlock",
		section = sectionSkills,
		position = 10
	)
	default boolean excludeConstruction()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeCooking",
		name = "Exclude Cooking XP",
		description = "Whether to disallow XP gained in the Cooking skill towards a new chunk unlock",
		section = sectionSkills,
		position = 11
	)
	default boolean excludeCooking()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeCrafting",
		name = "Exclude Crafting XP",
		description = "Whether to disallow XP gained in the Crafting skill towards a new chunk unlock",
		section = sectionSkills,
		position = 12
	)
	default boolean excludeCrafting()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeDefence",
		name = "Exclude Defence XP",
		description = "Whether to disallow XP gained in the Defence skill towards a new chunk unlock",
		section = sectionSkills,
		position = 13
	)
	default boolean excludeDefence()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeFarming",
		name = "Exclude Farming XP",
		description = "Whether to disallow XP gained in the Farming skill towards a new chunk unlock",
		section = sectionSkills,
		position = 14
	)
	default boolean excludeFarming()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeFiremaking",
		name = "Exclude Firemaking XP",
		description = "Whether to disallow XP gained in the Firemaking skill towards a new chunk unlock",
		section = sectionSkills,
		position = 15
	)
	default boolean excludeFiremaking()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeFishing",
		name = "Exclude Fishing XP",
		description = "Whether to disallow XP gained in the Fishing skill towards a new chunk unlock",
		section = sectionSkills,
		position = 16
	)
	default boolean excludeFishing()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeFletching",
		name = "Exclude Fletching XP",
		description = "Whether to disallow XP gained in the Fletching skill towards a new chunk unlock",
		section = sectionSkills,
		position = 17
	)
	default boolean excludeFletching()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeHerblore",
		name = "Exclude Herblore XP",
		description = "Whether to disallow XP gained in the Herblore skill towards a new chunk unlock",
		section = sectionSkills,
		position = 18
	)
	default boolean excludeHerblore()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeHitpoints",
		name = "Exclude Hitpoints XP",
		description = "Whether to disallow XP gained in the Hitpoints skill towards a new chunk unlock",
		section = sectionSkills,
		position = 19
	)
	default boolean excludeHitpoints()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeHunter",
		name = "Exclude Hunter XP",
		description = "Whether to disallow XP gained in the Hunter skill towards a new chunk unlock",
		section = sectionSkills,
		position = 20
	)
	default boolean excludeHunter()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeMagic",
		name = "Exclude Magic XP",
		description = "Whether to disallow XP gained in the Magic skill towards a new chunk unlock",
		section = sectionSkills,
		position = 21
	)
	default boolean excludeMagic()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeMining",
		name = "Exclude Mining XP",
		description = "Whether to disallow XP gained in the Mining skill towards a new chunk unlock",
		section = sectionSkills,
		position = 22
	)
	default boolean excludeMining()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludePrayer",
		name = "Exclude Prayer XP",
		description = "Whether to disallow XP gained in the Prayer skill towards a new chunk unlock",
		section = sectionSkills,
		position = 23
	)
	default boolean excludePrayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeRanged",
		name = "Exclude Ranged XP",
		description = "Whether to disallow XP gained in the Ranged skill towards a new chunk unlock",
		section = sectionSkills,
		position = 24
	)
	default boolean excludeRanged()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeRunecraft",
		name = "Exclude Runecraft XP",
		description = "Whether to disallow XP gained in the Runecraft skill towards a new chunk unlock",
		section = sectionSkills,
		position = 25
	)
	default boolean excludeRunecraft()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeSlayer",
		name = "Exclude Slayer XP",
		description = "Whether to disallow XP gained in the Slayer skill towards a new chunk unlock",
		section = sectionSkills,
		position = 26
	)
	default boolean excludeSlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeSmithing",
		name = "Exclude Smithing XP",
		description = "Whether to disallow XP gained in the Smithing skill towards a new chunk unlock",
		section = sectionSkills,
		position = 27
	)
	default boolean excludeSmithing()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeStrength",
		name = "Exclude Strength XP",
		description = "Whether to disallow XP gained in the Strength skill towards a new chunk unlock",
		section = sectionSkills,
		position = 28
	)
	default boolean excludeStrength()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeThieving",
		name = "Exclude Thieving XP",
		description = "Whether to disallow XP gained in the Thieving skill towards a new chunk unlock",
		section = sectionSkills,
		position = 29
	)
	default boolean excludeThieving()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeWoodcutting",
		name = "Exclude Woodcutting XP",
		description = "Whether to disallow XP gained in the Woodcutting skill towards a new chunk unlock",
		section = sectionSkills,
		position = 30
	)
	default boolean excludeWoodcutting()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showChunkNumber",
		name = "Show chunk number",
		description = "Whether to display a number on the world map indicating the order in which the chunk was unlocked",
		section = sectionDisplay,
		position = 31
	)
	default boolean showChunkNumber()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFruitMapIndicator",
		name = "Show fruit map indicator",
		description = "Whether to display a focus point indicator for the fruit chunk on the world map",
		section = sectionDisplay,
		position = 32
	)
	default boolean showFruitIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showInfo",
		name = "Show info box",
		description = "Whether to display info about number of unlocked chunks and xp until next unlock in an overlay",
		section = sectionDisplay,
		position = 33
	)
	default boolean showInfo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawOutlineOnly",
		name = "Draw only outline",
		description = "Whether to only draw the combined chunk border outline and not individual chunk borders",
		section = sectionDisplay,
		position = 34
	)
	default boolean drawOutlineOnly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "chunkBorderWidth",
		name = "Chunk border width",
		description = "The width of the chunk border in the game scene",
		section = sectionDisplay,
		position = 35
	)
	default double chunkBorderWidth()
	{
		return 2;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedBorderColourScene",
		name = "Unlocked border",
		description = "The colour of the unlocked chunk borders in the game scene",
		section = sectionScene,
		position = 36
	)
	default Color unlockedBorderColourScene()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedFillColourScene",
		name = "Unlocked fill",
		description = "The fill colour of the unlocked chunks in the game scene",
		section = sectionScene,
		position = 37
	)
	default Color unlockedFillColourScene()
	{
		return new Color(0, 255, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "lockedFillColourScene",
		name = "Locked fill",
		description = "The fill colour of the locked chunks in the game scene",
		section = sectionScene,
		position = 38
	)
	default Color lockedFillColourScene()
	{
		return new Color(20, 40, 80, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkBorderColourScene",
		name = "Fruit chunk border",
		description = "The colour of the snake fruit chunk borders in the game scene",
		section = sectionScene,
		position = 39
	)
	default Color fruitChunkBorderColourScene()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkFillColourScene",
		name = "Fruit chunk fill",
		description = "The fill colour of the snake fruit chunks in the game scene",
		section = sectionScene,
		position = 40
	)
	default Color fruitChunkFillColourScene()
	{
		return new Color(255, 0, 255, 64);
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedBorderColourMinimap",
		name = "Unlocked border",
		description = "The colour of the unlocked chunk borders on the minimap",
		section = sectionMinimap,
		position = 41
	)
	default Color unlockedBorderColourMinimap()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedFillColourMinimap",
		name = "Unlocked fill",
		description = "The fill colour of the unlocked chunks on the minimap",
		section = sectionMinimap,
		position = 42
	)
	default Color unlockedFillColourMinimap()
	{
		return new Color(0, 255, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "lockedFillColourMinimap",
		name = "Locked fill",
		description = "The fill colour of the locked chunks on the minimap",
		section = sectionMinimap,
		position = 43
	)
	default Color lockedFillColourMinimap()
	{
		return new Color(20, 40, 80, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkBorderColourMinimap",
		name = "Fruit chunk border",
		description = "The colour of the snake fruit chunk borders on the minimap",
		section = sectionMinimap,
		position = 44
	)
	default Color fruitChunkBorderColourMinimap()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkFillColourMinimap",
		name = "Fruit chunk fill",
		description = "The fill colour of the snake fruit chunks on the minimap",
		section = sectionMinimap,
		position = 45
	)
	default Color fruitChunkFillColourMinimap()
	{
		return new Color(255, 0, 255, 64);
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedBorderColourWorldmap",
		name = "Unlocked border",
		description = "The colour of the unlocked chunk borders on the world map",
		section = sectionWorldmap,
		position = 46
	)
	default Color unlockedBorderColourWorldmap()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		keyName = "unlockedFillColourWorldmap",
		name = "Unlocked fill",
		description = "The fill colour of the unlocked chunks on the world map",
		section = sectionWorldmap,
		position = 47
	)
	default Color unlockedFillColourWorldmap()
	{
		return new Color(0, 255, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "lockedFillColourWorldmap",
		name = "Locked fill",
		description = "The fill colour of the locked chunks on the world map",
		section = sectionWorldmap,
		position = 48
	)
	default Color lockedFillColourWorldmap()
	{
		return new Color(20, 40, 80, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkBorderColourWorldmap",
		name = "Fruit chunk border",
		description = "The colour of the snake fruit chunk borders on the world map",
		section = sectionWorldmap,
		position = 49
	)
	default Color fruitChunkBorderColourWorldmap()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
		keyName = "fruitChunkFillColourWorldmap",
		name = "Fruit chunk fill",
		description = "The fill colour of the snake fruit chunks on the world map",
		section = sectionWorldmap,
		position = 50
	)
	default Color fruitChunkFillColourWorldmap()
	{
		return new Color(255, 0, 255, 64);
	}

	@ConfigItem(
		keyName = SnakemanModePlugin.CONFIG_RESET_KEY,
		name = "Last unlock xp",
		description = "The number of overall experience at the time of the last unlock",
		position = 51,
		hidden = true
	)
	default long lastUnlockXp()
	{
		return SnakemanModePlugin.CONFIG_RESET_VALUE;
	}

	@ConfigItem(
		keyName = "lastUnlockXp",
		name = "Set last unlock xp",
		description = "",
		position = 52
	)
	void lastUnlockXp(long xp);

	@ConfigItem(
		keyName = "unlockProgress",
		name = "Unlock progress",
		description = "The number of experience gained towards the next unlock",
		position = 53,
		hidden = true
	)
	default long unlockProgress()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "unlockProgress",
		name = "Set unlock progress",
		description = "",
		position = 54
	)
	void unlockProgress(long xp);
}
