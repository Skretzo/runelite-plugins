package com.successrates.trackers;

import com.successrates.SuccessRatesTracker;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

/*
 * This approach will produce skewed data if you cancel a firemaking attempt
 */
public class LightingLogs extends SuccessRatesTracker
{
	private static final int IDLE = AnimationID.IDLE;
	private static final int FIREMAKING = AnimationID.FIREMAKING;
	private static final int FIREMAKING_DURATION = 4;
	private static final int FIREMAKING_DURATION_FIRST = 3;
	private static final int FIREMAKING_CHAT_MESSAGE_OFFSET = 1;
	private static final String FIREMAKING_CHAT_MESSAGE = "The fire catches and the logs begin to burn.";

	private int lastId;
	private int lastTick;

	private int lastLevel;
	private int lastSuccesses;
	private int lastFailures;

	@Override
	public Skill getSkill()
	{
		return Skill.FIREMAKING;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (client == null || plugin == null)
		{
			return;
		}
		Actor actor = event.getActor();
		Actor player = client.getLocalPlayer();

		if (player == null || !player.equals(actor))
		{
			return;
		}

		final int id = actor.getAnimation();
		final int tick = client.getTickCount();
		final int lvl = Math.min(99, client.getBoostedSkillLevel(Skill.FIREMAKING));

		final int deltaTick = tick - lastTick;

		if (id == IDLE && lastId == IDLE && deltaTick == FIREMAKING_DURATION)
		{
			// Success when firemaking in a chain
			lastLevel = lvl;
			lastSuccesses = 1;
			lastFailures = 0;
		}
		else if (id == IDLE && lastId == FIREMAKING && deltaTick >= FIREMAKING_DURATION_FIRST)
		{
			if (((deltaTick - FIREMAKING_DURATION_FIRST) % FIREMAKING_DURATION) == 0)
			{
				// Fail(s) into a success when firemaking from a proper idle state
				final int fails = (deltaTick - FIREMAKING_DURATION_FIRST) / FIREMAKING_DURATION;
				lastLevel = lvl;
				lastSuccesses = 1;
				lastFailures = fails;
			}
			else if ((deltaTick % FIREMAKING_DURATION) == 0)
			{
				// Fail(s) into a success when firemaking in a chain
				final int fails = deltaTick / FIREMAKING_DURATION;
				lastLevel = lvl;
				lastSuccesses = 1;
				lastFailures = fails;
			}
		}

		lastId = id;
		lastTick = tick;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		final int deltaTick = client.getTickCount() - lastTick;

		if (deltaTick == FIREMAKING_CHAT_MESSAGE_OFFSET && ChatMessageType.SPAM.equals(event.getType()) &&
			FIREMAKING_CHAT_MESSAGE.equals(event.getMessage()))
		{
			update(lastLevel, lastSuccesses, lastFailures);
		}
	}
}
