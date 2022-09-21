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
public class Pickpocketing extends ChatSuccessRatesTracker
{
	private final String target;

	private String getMessageSuccess()
	{
		return "You pick the " + target + "'s pocket";
	}

	private String getMessageFailure()
	{
		return "You fail to pick the " + target + "'s pocket.";
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.THIEVING;
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

		if (message.startsWith(getMessageSuccess()))
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
		return "Pickpocketing" + StringUtils.deleteWhitespace(WordUtils.capitalize(target));
	}
}
