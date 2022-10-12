package com.transcriber;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("transcriber")
public interface TranscriberConfig extends Config
{
	@ConfigItem(
		keyName = "animationIds",
		name = "Include animation IDs",
		description = "Whether to include animation IDs in the transcript, e.g. <animationID=100>"
	)
	default boolean animationIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fontIds",
		name = "Include font IDs",
		description = "Whether to include font IDs in the transcript, e.g. <fontID=497>"
	)
	default boolean fontIds()
	{
		return false;
	}

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
		keyName = "modelIds",
		name = "Include model IDs",
		description = "Whether to include model IDs in the transcript, e.g. <modelID=11365>"
	)
	default boolean modelIds()
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
		return "4,7,65,69,76,77,84,94,109,116,122,134,137,156,160,162,182,193,201,216,217," +
			"218,219,229,231,238,239,245,259,278,310,320,370,372,378,387,399,429,432,464," +
			"527,541,553,593,595,600,621,626,629,701,702,707,712,713,714,715,716,717";
	}
}
