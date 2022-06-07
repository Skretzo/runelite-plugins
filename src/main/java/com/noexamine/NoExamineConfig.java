package com.noexamine;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("noexamine")
public interface NoExamineConfig extends Config
{
	@ConfigItem(
		keyName = "itemGround",
		name = "Ground items",
		description = "Whether to remove examine menu options on ground items"
	)
	default boolean itemsGround()
	{
		return true;
	}

	@ConfigItem(
		keyName = "itemsInventory",
		name = "Inventory items",
		description = "Whether to remove examine menu options on inventory items"
	)
	default boolean itemInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "npcs",
		name = "NPCs",
		description = "Whether to remove examine menu options on NPCs"
	)
	default boolean npcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "objects",
		name = "Objects",
		description = "Whether to remove examine menu options on objects"
	)
	default boolean objects()
	{
		return true;
	}
}
