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

	@ConfigItem(
		keyName = "widgetBlacklist",
		name = "Blacklist",
		description = "A list of widget group IDs to exclude",
		position = 100
	)
	default String widgetBlacklist()
	{
		return "4,7,65,69,76,77,84,94,122,134,137,156,160,162,182,193,201,217,219,229,231,239,245,259,278,310," +
			"320,370,372,399,464,527,541,553,593,595,600,621,626,629,701,702,707,712,713,714,715,716,717";
	}
}
