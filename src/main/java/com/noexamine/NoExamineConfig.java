package com.noexamine;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("noexamine")
public interface NoExamineConfig extends Config
{
	@ConfigSection(
		name = "Examine",
		description = "Settings for removing examine menu options",
		position = 0
	)
	String sectionExamine = "sectionExamine";

	@ConfigItem(
		keyName = "examineShift",
		name = "Bypass with Shift",
		description = "Whether to not remove examine menu options when Shift is held down",
		position = 1,
		section = sectionExamine
	)
	default boolean examineShift()
	{
		return true;
	}

	@ConfigItem(
		keyName = "examineItemGround",
		name = "Ground items",
		description = "Whether to remove examine menu options on ground items",
		position = 2,
		section = sectionExamine
	)
	default boolean examineItemsGround()
	{
		return true;
	}

	@ConfigItem(
		keyName = "examineItemsInventory",
		name = "Inventory items",
		description = "Whether to remove examine menu options on inventory items",
		position = 3,
		section = sectionExamine
	)
	default boolean examineItemInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "examineNpcs",
		name = "NPCs",
		description = "Whether to remove examine menu options on NPCs",
		position = 4,
		section = sectionExamine
	)
	default boolean examineNpcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "examineObjects",
		name = "Objects",
		description = "Whether to remove examine menu options on objects",
		position = 5,
		section = sectionExamine
	)
	default boolean examineObjects()
	{
		return true;
	}

	@ConfigSection(
		name = "Cancel",
		description = "Settings for removing cancel menu options",
		position = 6
	)
	String sectionCancel = "sectionCancel";

	@ConfigItem(
		keyName = "cancelEverywhere",
		name = "Everywhere",
		description = "Whether to remove cancel menu options everywhere",
		position = 7,
		section = sectionCancel
	)
	default boolean cancelEverywhere()
	{
		return false;
	}
}
