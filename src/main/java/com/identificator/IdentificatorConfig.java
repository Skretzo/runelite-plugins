package com.identificator;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(IdentificatorPlugin.CONFIG_GROUP)
public interface IdentificatorConfig extends Config
{
	@ConfigItem(
		keyName = "showHoverInfo",
		name = "Show hover info",
		description = "Whether to show identification info on mouse hover",
		position = 0
	)
	default boolean showHoverInfo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOverheadInfo",
		name = "Show overhead info",
		description = "Whether to show identification info above NPCs and objects",
		position = 1
	)
	default boolean showOverheadInfo()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showMenuInfo",
		name = "Show menu info",
		description = "Whether to append identification info in menus",
		position = 2
	)
	default boolean showMenuInfo()
	{
		return true;
	}

	@ConfigItem(
		keyName = "triggerWithShift",
		name = "Trigger with shift",
		description = "Whether the identification info should only be triggered when the shift key is held down",
		position = 3
	)
	default boolean triggerWithShift()
	{
		return false;
	}

	@ConfigSection(
		name = "Options",
		description = "Options for which IDs to display",
		position = 4
	)
	String optionsSection = "optionsSection";

	@ConfigItem(
		keyName = "showNpcId",
		name = "Show NPC id",
		description = "Whether to display the id of an NPC",
		position = 5,
		section = optionsSection
	)
	default boolean showNpcId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNpcAnimationId",
		name = "Show NPC animation id",
		description = "Whether to display the current animation id of an NPC",
		position = 6,
		section = optionsSection
	)
	default boolean showNpcAnimationId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNpcPoseAnimationId",
		name = "Show NPC pose animation id",
		description = "Whether to display the current pose animation id of an NPC",
		position = 7,
		section = optionsSection
	)
	default boolean showNpcPoseAnimationId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNpcGraphicId",
		name = "Show NPC graphic id",
		description = "Whether to display the current graphic id of an NPC",
		position = 8,
		section = optionsSection
	)
	default boolean showNpcGraphicId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPlayerAnimationId",
		name = "Show player animation id",
		description = "Whether to display the current animation id of a player",
		position = 9,
		section = optionsSection
	)
	default boolean showPlayerAnimationId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPlayerPoseAnimationId",
		name = "Show player pose animation id",
		description = "Whether to display the current pose animation id of a player",
		position = 10,
		section = optionsSection
	)
	default boolean showPlayerPoseAnimationId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPlayerGraphicId",
		name = "Show player graphic id",
		description = "Whether to display the current graphic id of a player",
		position = 11,
		section = optionsSection
	)
	default boolean showPlayerGraphicId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGameObjectId",
		name = "Show game object id",
		description = "Whether to display the id of a game object",
		position = 12,
		section = optionsSection
	)
	default boolean showGameObjectId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGroundObjectId",
		name = "Show ground object id",
		description = "Whether to display the id of a ground object",
		position = 13,
		section = optionsSection
	)
	default boolean showGroundObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showDecorativeObjectId",
		name = "Show decorative object id",
		description = "Whether to display the id of a decorative object",
		position = 14,
		section = optionsSection
	)
	default boolean showDecorativeObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWallObjectId",
		name = "Show wall object id",
		description = "Whether to display the id of a wall object",
		position = 15,
		section = optionsSection
	)
	default boolean showWallObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundItemId",
		name = "Show ground item id",
		description = "Whether to display the id of a ground item",
		position = 16,
		section = optionsSection
	)
	default boolean showGroundItemId()
	{
		return true;
	}

	@ConfigSection(
		name = "Colours",
		description = "Options for the text colours",
		position = 17
	)
	String coloursSection = "coloursSection";

	@Alpha
	@ConfigItem(
		keyName = "colourHover",
		name = "Hover text",
		description = "The colour of the hover info text",
		position = 18
	)
	default Color colourHover()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		keyName = "colourOverhead",
		name = "Overhead text",
		description = "The colour of the overhead info text",
		position = 19
	)
	default Color colourOverhead()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		keyName = "colourMenu",
		name = "Menu text",
		description = "The colour of the menu info text",
		position = 20
	)
	default Color colourMenu()
	{
		return new Color(170, 110, 0);
	}
}
