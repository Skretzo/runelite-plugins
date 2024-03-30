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
		keyName = "showNpcMorphId",
		name = "Show NPC morph id",
		description = "Whether to display the current transformed/morph id of an NPC",
		position = 6,
		section = optionsSection
	)
	default boolean showNpcMorphId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showNpcAnimationId",
		name = "Show NPC animation id",
		description = "Whether to display the current animation id of an NPC",
		position = 7,
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
		position = 8,
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
		position = 9,
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
		position = 10,
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
		position = 11,
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
		position = 12,
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
		position = 13,
		section = optionsSection
	)
	default boolean showGameObjectId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGameObjectMorphId",
		name = "Show game object morph id",
		description = "Whether to display the current transformed/morph id of a game object",
		position = 14,
		section = optionsSection
	)
	default boolean showGameObjectMorphId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGameObjectAnimationId",
		name = "Show game object animation id",
		description = "Whether to display the animation id of a dynamic game object.<br>" +
			"Make sure to hover/interact with the base tile(s) of the object.",
		position = 15,
		section = optionsSection
	)
	default boolean showGameObjectAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundObjectId",
		name = "Show ground object id",
		description = "Whether to display the id of a ground object",
		position = 16,
		section = optionsSection
	)
	default boolean showGroundObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundObjectMorphId",
		name = "Show ground object morph id",
		description = "Whether to display the current transformed/morph id of a ground object",
		position = 17,
		section = optionsSection
	)
	default boolean showGroundObjectMorphId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundObjectAnimationId",
		name = "Show ground object animation id",
		description = "Whether to display the animation id of a dynamic ground object",
		position = 18,
		section = optionsSection
	)
	default boolean showGroundObjectAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showDecorativeObjectId",
		name = "Show decorative object id",
		description = "Whether to display the id of a decorative object",
		position = 19,
		section = optionsSection
	)
	default boolean showDecorativeObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showDecorativeObjectMorphId",
		name = "Show decorative object morph id",
		description = "Whether to display the current transformed/morph id of a decorative object",
		position = 20,
		section = optionsSection
	)
	default boolean showDecorativeObjectMorphId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showDecorativeObjectAnimationId",
		name = "Show decorative object animation id",
		description = "Whether to display the animation id of a dynamic decorative object",
		position = 21,
		section = optionsSection
	)
	default boolean showDecorativeObjectAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWallObjectId",
		name = "Show wall object id",
		description = "Whether to display the id of a wall object",
		position = 22,
		section = optionsSection
	)
	default boolean showWallObjectId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWallObjectMorphId",
		name = "Show wall object morph id",
		description = "Whether to display the current transformed/morph id of a wall object",
		position = 23,
		section = optionsSection
	)
	default boolean showWallObjectMorphId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWallObjectAnimationId",
		name = "Show wall object animation id",
		description = "Whether to display the animation id of a dynamic wall object",
		position = 24,
		section = optionsSection
	)
	default boolean showWallObjectAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGroundItemId",
		name = "Show ground item id",
		description = "Whether to display the id of a ground item",
		position = 25,
		section = optionsSection
	)
	default boolean showGroundItemId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showNpcOverrideModelIds",
		name = "Show NPC override model ids",
		description = "Whether to display NPC override model ids (M)",
		position = 26,
		section = optionsSection
	)
	default boolean showNpcOverrideModelIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showNpcOverrideColours",
		name = "Show NPC override model colours",
		description = "Whether to display NPC override model colours (C)",
		position = 27,
		section = optionsSection
	)
	default boolean showNpcOverrideColours()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showNpcOverrideTextures",
		name = "Show NPC override model textures",
		description = "Whether to display NPC override model textures (T)",
		position = 28,
		section = optionsSection
	)
	default boolean showNpcOverrideTextures()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showNpcChatheadModelId",
		name = "Show NPC chathead model ID",
		description = "Whether to display NPC chathead model ID",
		position = 29,
		section = optionsSection
	)
	default boolean showNpcChatheadModelId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showNpcChatheadAnimationId",
		name = "Show NPC chathead animation ID",
		description = "Whether to display NPC chathead animation ID",
		position = 30,
		section = optionsSection
	)
	default boolean showNpcChatheadAnimationId()
	{
		return false;
	}

	@ConfigSection(
		name = "Logging",
		description = "Options for logging tick timestamp and player related IDs in a side panel",
		position = 31
	)
	String loggingSection = "optionsLogging";

	@ConfigItem(
		keyName = "logging",
		name = "Enable logging",
		description = "Whether to enable logging of tick timestamp and player related IDs in a side panel",
		position = 32,
		section = loggingSection
	)
	default boolean logging()
	{
		return false;
	}

	@ConfigItem(
		keyName = "logRelativeTickTimestamp",
		name = "Relative tick timestamp",
		description = "Whether to make the tick timestamp relative to the last logged entry or make it static",
		position = 33,
		section = loggingSection
	)
	default boolean logRelativeTickTimestamp()
	{
		return false;
	}

	@ConfigItem(
		keyName = "logPlayerAnimationId",
		name = "Log animation IDs",
		description = "Whether to log player animation IDs in a side panel",
		position = 34,
		section = loggingSection
	)
	default boolean logPlayerAnimationId()
	{
		return true;
	}

	@ConfigItem(
		keyName = "logPlayerPoseAnimationId",
		name = "Log pose animation IDs",
		description = "Whether to log player pose animation IDs in a side panel",
		position = 35,
		section = loggingSection
	)
	default boolean logPlayerPoseAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "logPlayerGraphicId",
		name = "Log graphic IDs",
		description = "Whether to log player graphic IDs in a side panel",
		position = 36,
		section = loggingSection
	)
	default boolean logPlayerGraphicId()
	{
		return false;
	}

	@ConfigSection(
		name = "Colours",
		description = "Options for the text colours",
		position = 37
	)
	String coloursSection = "coloursSection";

	@Alpha
	@ConfigItem(
		keyName = "colourHover",
		name = "Hover text",
		description = "The colour of the hover info text",
		position = 38,
		section = coloursSection
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
		position = 39,
		section = coloursSection
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
		position = 40,
		section = coloursSection
	)
	default Color colourMenu()
	{
		return new Color(170, 110, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourChathead",
		name = "Chathead text",
		description = "The colour of the chathead info text",
		position = 41,
		section = coloursSection
	)
	default Color colourChathead()
	{
		return Color.BLACK;
	}
}
