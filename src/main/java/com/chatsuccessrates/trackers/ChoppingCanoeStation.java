package com.chatsuccessrates.trackers;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;

@RequiredArgsConstructor
public class ChoppingCanoeStation extends ChatSuccessRatesTracker
{
	private static final int WOODCUTTING_SPEED = 4;

	private static final List<MenuAction> OBJECT_MENU_ACTIONS = ImmutableList.of(
		MenuAction.GAME_OBJECT_FIRST_OPTION,
		MenuAction.GAME_OBJECT_SECOND_OPTION,
		MenuAction.GAME_OBJECT_THIRD_OPTION,
		MenuAction.GAME_OBJECT_FOURTH_OPTION,
		MenuAction.GAME_OBJECT_FIFTH_OPTION
	);

	private final String axe;
	private final int animationId;
	private int lastAnimationId = -2;
	private int lastObjectId = -2;
	private int start = 0;

	private int getAnimationIdAttempt()
	{
		return animationId;
	}

	private int getAnimationIdSuccess()
	{
		return -1;
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.WOODCUTTING;
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (OBJECT_MENU_ACTIONS.contains(event.getMenuAction()))
		{
			lastObjectId = getMorphedObjectId(event.getId());
		}
	}

	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
		if (lastObjectId != ObjectID.CANOESTATION_TREE)
		{
			return;
		}

		final Player player = client.getLocalPlayer();
		if (player == null || !player.equals(event.getActor()))
		{
			return;
		}

		final int animationId = player.getAnimation();
		
		if (animationId == getAnimationIdAttempt())
		{
			start = client.getTickCount();
		}
		else if (lastAnimationId == getAnimationIdAttempt() && animationId == getAnimationIdSuccess())
		{
			final int level = client.getBoostedSkillLevel(getSkill().getSkill());
			int fails = (client.getTickCount() - start - 1) / WOODCUTTING_SPEED;
			update(level, 1, fails);
		}

		lastAnimationId = animationId;
	}

	private int getMorphedObjectId(int objectId)
	{
		if (objectId < 0)
		{
			return objectId;
		}
		ObjectComposition objectComposition = client.getObjectDefinition(objectId);
		if (objectComposition != null && objectComposition.getImpostorIds() != null)
		{
			ObjectComposition imposterObjectComposition = objectComposition.getImpostor();
			if (imposterObjectComposition != null)
			{
				return imposterObjectComposition.getId();
			}
		}
		return objectId;
	}

	@Override
	public String getTrackerName()
	{
		return "ChoppingCanoeStation" + StringUtils.deleteWhitespace(WordUtils.capitalize(axe));
	}
}
