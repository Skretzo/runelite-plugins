package com.chatsuccessrates;

import net.runelite.api.Skill;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chatsuccessrates")
public interface ChatSuccessRatesConfig extends Config
{
	@ConfigItem(
		keyName = "addLevelPrefix",
		name = "Add level prefix",
		description = "Whether to add a skill level prefix/identifier to the tracked chat messages",
		position = 0
	)
	default boolean addLevelPrefix()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useBoostedLevel",
		name = "Use boosted level",
		description = "Whether to use the boosted skill level (numerator) or the static base skill level (denominator)",
		position = 1
	)
	default boolean useBoostedLevel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "levelPrefix",
		name = "Level prefix",
		description = "The skill level prefix/identifier on the tracked chat messages",
		position = 2
	)
	default Skill levelPrefix()
	{
		return Skill.OVERALL;
	}

	@ConfigItem(
		keyName = "messageSuccess",
		name = "Success message",
		description = "The chat message used to track a skilling action success",
		position = 3
	)
	default String messageSuccess()
	{
		return "";
	}

	@ConfigItem(
		keyName = "messageFailure",
		name = "Failure message",
		description = "The chat message used to track a skilling action failure",
		position = 4
	)
	default String messageFailure()
	{
		return "";
	}
}
