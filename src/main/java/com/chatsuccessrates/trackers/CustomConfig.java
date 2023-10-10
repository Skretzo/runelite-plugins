package com.chatsuccessrates.trackers;

import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import java.awt.Color;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.SkillColor;
import static com.chatsuccessrates.ChatSuccessRatesPlugin.COLLAPSIBLE_MESSAGETYPES;

public class CustomConfig extends ChatSuccessRatesTracker
{
	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.CUSTOM;
	}

	@Override
	public Color getColor()
	{
		return config == null
			? super.getColor()
			: (ChatSuccessRatesSkill.CUSTOM.equals(config.levelPrefix())
				? Color.RED
				: SkillColor.find(config.levelPrefix().getSkill()).getColor());
	}

	@Override
	public String getTrackerName()
	{
		return "Config";
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (config == null || !COLLAPSIBLE_MESSAGETYPES.contains(event.getType()))
		{
			return;
		}

		final String message = event.getMessage();
		final ChatSuccessRatesSkill skill = config.levelPrefix();
		final int level = ChatSuccessRatesSkill.CUSTOM.equals(skill) ? client.getTotalLevel() :
			(config.useBoostedLevel() ? client.getBoostedSkillLevel(skill.getSkill()) : client.getRealSkillLevel(skill.getSkill()));

		if (config.messageSuccess().equals(message))
		{
			update(level, 1, 0);
		}
		else if (config.messageFailure().equals(message))
		{
			update(level, 0, 1);
		}
	}
}
