package com.chatsuccessrates.trackers;

import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import static com.chatsuccessrates.ChatSuccessRatesPlugin.COLLAPSIBLE_MESSAGETYPES;

@RequiredArgsConstructor
public class CatchingPetfish extends ChatSuccessRatesTracker
{
	public static final String TYPE_BLUE = "Bluefish";
	public static final String TYPE_GREEN = "Greenfish";
	public static final String TYPE_SPINE = "Spinefish";

	private final String type;

	private String getMessageSuccess(String type)
	{
		return "...and catch a Tiny " + type + "!";
	}

	private Set<String> getMessagesFailure()
	{
		Set<String> failures = new HashSet<>();
		for (String type : new String[] { TYPE_BLUE, TYPE_GREEN, TYPE_SPINE })
		{
			if (!type.equals(this.type))
			{
				failures.add(getMessageSuccess(type));
			}
		}
		return failures;
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.FISHING;
	}

	@Override
	public Color getColor()
	{
		switch (type)
		{
			default:
				return new Color(27, 165, 227);
			case TYPE_GREEN:
				return new Color(0, 255, 0);
			case TYPE_SPINE:
				return new Color(255, 150, 0);
		}
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!COLLAPSIBLE_MESSAGETYPES.contains(event.getType()))
		{
			return;
		}

		final String message = event.getMessage();
		final int level = (config != null && config.useBoostedLevel())
			? client.getBoostedSkillLevel(getSkill().getSkill())
			: client.getRealSkillLevel(getSkill().getSkill());

		if (getMessageSuccess(type).equals(message))
		{
			update(level, 1, 0);
		}
		else if (getMessagesFailure().contains(message))
		{
			update(level, 0, 1);
		}
	}

	@Override
	public String getTrackerName()
	{
		return "CatchingTiny" + StringUtils.deleteWhitespace(WordUtils.capitalize(type)) + "Pets";
	}
}
