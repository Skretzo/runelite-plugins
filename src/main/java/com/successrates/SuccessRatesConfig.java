package com.successrates;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("successrates")
public interface SuccessRatesConfig extends Config
{
	@ConfigItem(
		keyName = "currentSkill",
		name = "Current skill",
		description = "The current skill that is being displayed",
		position = 0,
		hidden = true
	)
	default int indexSkill()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "currentTracker",
		name = "Current tracker",
		description = "The current tracker that is being displayed",
		position = 1,
		hidden = true
	)
	default int indexTracker()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "currentSkill",
		name = "Set current skill",
		description = ""
	)
	void indexSkill(int idx);

	@ConfigItem(
		keyName = "currentTracker",
		name = "Set current tracker",
		description = ""
	)
	void indexTracker(int idx);
}
