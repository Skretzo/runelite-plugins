package com.chatsuccessrates.trackers;

import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

@RequiredArgsConstructor
public class FiringPottery extends ChatSuccessRatesTracker
{
	private final String item;

	private String getMessageSuccess()
	{
		return "You remove the " + item + " from the oven.";
	}

	private String getMessageFailure()
	{
		return "The " + item + " cracks in the oven.";
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.CRAFTING;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!ChatMessageType.SPAM.equals(event.getType()))
		{
			return;
		}

		final String message = event.getMessage();
		final int level = client.getBoostedSkillLevel(getSkill().getSkill());

		if (getMessageSuccess().equals(message))
		{
			update(level, 1, 0);
		}
		else if (getMessageFailure().equals(message))
		{
			update(level, 0, 1);
		}
	}

	@Override
	public String getTrackerName()
	{
		return "Firing" + StringUtils.deleteWhitespace(WordUtils.capitalize(item));
	}
}
