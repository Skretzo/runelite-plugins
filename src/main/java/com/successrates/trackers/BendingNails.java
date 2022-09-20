package com.successrates.trackers;

import com.successrates.SuccessRatesSkill;
import com.successrates.SuccessRatesTracker;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

public class BendingNails extends SuccessRatesTracker
{
	private static final String BENDING_SUCCESS = "You accidentally bend a nail.";
	private static final String BENDING_FAILURE = "You use a nail.";

	@Override
	public SuccessRatesSkill getSkill()
	{
		return SuccessRatesSkill.CONSTRUCTION;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!ChatMessageType.SPAM.equals(event.getType()))
		{
			return;
		}

		final String message = event.getMessage();
		final int level = Math.min(99, client.getBoostedSkillLevel(getSkill().getSkill()));

		if (BENDING_SUCCESS.equals(message))
		{
			update(level, 1, 0);
		}
		else if (BENDING_FAILURE.equals(message))
		{
			update(level, 0, 1);
		}
	}
}
