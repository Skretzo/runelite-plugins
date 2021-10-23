package com.successrates.trackers;

import com.successrates.SuccessRatesTracker;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

public class FiringBowl extends SuccessRatesTracker
{
	private static final String ITEM = "bowl";
	private static final String FIRING_SUCCESS = "You remove the " + ITEM + " from the oven.";
	private static final String FIRING_FAILURE = "The " + ITEM + " cracks in the oven.";

	@Override
	public Skill getSkill()
	{
		return Skill.CRAFTING;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!ChatMessageType.SPAM.equals(event.getType()))
		{
			return;
		}

		final String message = event.getMessage();
		final int level = Math.min(99, client.getBoostedSkillLevel(Skill.CRAFTING));

		if (FIRING_SUCCESS.equals(message))
		{
			update(level, 1, 0);
		}
		else if (FIRING_FAILURE.equals(message))
		{
			update(level, 0, 1);
		}
	}
}
