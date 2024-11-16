package com.chatsuccessrates.trackers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

@RequiredArgsConstructor
public class PickingLock extends ChatSuccessRatesTracker
{
	private final String objectSuccess;
	private final String object;
	private final int objectId;

	private String getMessageSuccess()
	{
		return "You pick the lock on the " + objectSuccess + ".";
	}

	private String getMessageAttempt()
	{
		return "You attempt to pick the lock on the " + object + ".";
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

		if (getMessageAttempt().equals(message))
		{
			update(level, 0, 1);
		}
		else if (getMessageSuccess().equals(message))
		{
			// -1 fails because the last attempt is a success
			update(level, 1, -1);
		}
	}

	@Override
	public String getTrackerName()
	{
		return "Picking" + StringUtils.deleteWhitespace(WordUtils.capitalize(object)) + objectId + "Lock";
	}
}
