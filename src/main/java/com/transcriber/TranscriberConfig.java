package com.transcriber;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("transcriber")
public interface TranscriberConfig extends Config
{
	@ConfigItem(
		keyName = "itemIds",
		name = "Include item IDs",
		description = "Whether to include item IDs in the transcript, e.g. <itemID=4151>"
	)
	default boolean itemIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "spriteIds",
		name = "Include sprite IDs",
		description = "Whether to include sprite IDs in the transcript, e.g. <spriteID=537>"
	)
	default boolean spriteIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "removeUnnecessaryTags",
		name = "Remove unnecessary colour tags",
		description = "Whether to exclude unnecessary black colour tags, e.g. <col=000000>"
	)
	default boolean removeUnnecessaryTags()
	{
		return true;
	}
}
