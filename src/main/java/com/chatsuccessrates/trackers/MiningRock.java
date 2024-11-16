package com.chatsuccessrates.trackers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

@RequiredArgsConstructor
public class MiningRock extends ChatSuccessRatesTracker
{
	private static final String ATTEMPT = "You swing your pick at the rock.";

	private final String ore;
	private int start = 0;

	private String getMessageSuccess()
	{
		return "You manage to mine some " + ore + ".";
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.MINING;
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

		if (ATTEMPT.equals(message))
		{
			start = client.getTickCount();
		}
		else if (getMessageSuccess().equals(message))
		{
			// -1 for arrive delay, but does not change number of fails when
			// there is no arrive delay because no pickaxe has 1 tick interval
			int fails = (client.getTickCount() - start - 1) / getPickaxeInterval(level);
			update(level, 1, fails);
		}
	}

	@Override
	public String getTrackerName()
	{
		return "Mining" + StringUtils.deleteWhitespace(WordUtils.capitalize(ore));
	}

	private int getPickaxeInterval(int level)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (inventory == null && equipment == null)
		{
			return -1;
		}
		for (Pickaxe pickaxe : Pickaxe.values())
		{
			if (level < pickaxe.level)
			{
				continue;
			}
			for (int itemId : pickaxe.itemIds)
			{
				if ((inventory != null && inventory.find(itemId) >= 0) ||
					(equipment != null && equipment.find(itemId) >= 0))
				{
					return pickaxe.interval;
				}
			}
		}
		return -1;
	}

	private static enum Pickaxe
	{
		CRYSTAL(71, 3, ItemID.CRYSTAL_PICKAXE, ItemID.CRYSTAL_PICKAXE_23863),
		DRAGON(61, 3, ItemID.DRAGON_PICKAXE, ItemID.DRAGON_PICKAXE_12797, ItemID.DRAGON_PICKAXE_OR, ItemID.DRAGON_PICKAXE_OR_25376),
		GILDED(41, 3, ItemID.GILDED_PICKAXE),
		RUNE(41, 3, ItemID.RUNE_PICKAXE),
		ADAMANT(31, 4, ItemID.ADAMANT_PICKAXE),
		MITHRIL(21, 5, ItemID.MITHRIL_PICKAXE),
		BLACK(11, 5, ItemID.BLACK_PICKAXE),
		STEEL(6, 6, ItemID.STEEL_PICKAXE),
		IRON(1, 7, ItemID.IRON_PICKAXE),
		BRONZE(1, 8, ItemID.BRONZE_PICKAXE),
		;

		private final int level;
		private final int interval;
		private final int[] itemIds;

		Pickaxe(int level, int interval, int... itemIds)
		{
			this.level = level;
			this.interval = interval;
			this.itemIds = itemIds;
		}
	}
}
