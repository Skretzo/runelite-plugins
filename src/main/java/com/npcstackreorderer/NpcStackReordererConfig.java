package com.npcstackreorderer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("npcstackreorderer")
public interface NpcStackReordererConfig extends Config
{
	@ConfigItem(
		keyName = "hidePlayers",
		name = "Hide players",
		description = "Whether to hide players on NPC stacks that can reveal hidden NPCs",
		position = 0
	)
	default boolean hidePlayers()
	{
		return false;
	}
}
