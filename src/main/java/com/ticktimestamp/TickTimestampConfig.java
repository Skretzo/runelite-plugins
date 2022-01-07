package com.ticktimestamp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ticktimestamp")
public interface TickTimestampConfig extends Config
{
	@ConfigItem(
		keyName = "deltaTick",
		name = "Relative tick",
		description = "Whether to display the tick timestamp as a tick count relative to the previous timestamped chat message"
	)
	default boolean deltaTick()
	{
		return false;
	}
}
