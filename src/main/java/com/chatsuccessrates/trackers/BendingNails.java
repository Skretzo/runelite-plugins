package com.chatsuccessrates.trackers;

import com.chatsuccessrates.ChatSuccessRatesSkill;
import com.chatsuccessrates.ChatSuccessRatesTracker;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import static com.chatsuccessrates.ChatSuccessRatesPlugin.COLLAPSIBLE_MESSAGETYPES;

public class BendingNails extends ChatSuccessRatesTracker
{
	private static final String NAIL_SUCCESS = "You use a nail.";
	private static final String NAIL_FAILURE = "You accidentally bend a nail.";

	private final int nailType;

	private int nailQuantity = 0;
	private String message = null;
	private int messageTick;

	public BendingNails(int nailType)
	{
		this.nailType = nailType;
		updateNailQuantity();
	}

	@Override
	public ChatSuccessRatesSkill getSkill()
	{
		return ChatSuccessRatesSkill.CONSTRUCTION;
	}

	@Override
	public String getTrackerName()
	{
		final String PREFIX = "Bending";
		final String SUFFIX = "Nails";
		switch (nailType)
		{
			case ItemID.BRONZE_NAILS:
				return PREFIX + "Bronze" + SUFFIX;
			case ItemID.IRON_NAILS:
				return PREFIX + "Iron" + SUFFIX;
			case ItemID.STEEL_NAILS:
				return PREFIX + "Steel" + SUFFIX;
			case ItemID.BLACK_NAILS:
				return PREFIX + "Black" + SUFFIX;
			case ItemID.MITHRIL_NAILS:
				return PREFIX + "Mithril" + SUFFIX;
			case ItemID.ADAMANTITE_NAILS:
				return PREFIX + "Adamantite" + SUFFIX;
			case ItemID.RUNE_NAILS:
				return PREFIX + "Rune" + SUFFIX;
		}
		return PREFIX + "Unknown" + SUFFIX;
	}

	private int crystalSawBoost()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null && inventory.contains(ItemID.CRYSTAL_SAW))
		{
			return 3;
		}
		return 0;
	}

	private int updateNailQuantity()
	{
		if (client == null)
		{
			return 0;
		}

		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null || !inventory.contains(nailType))
		{
			return 0;
		}

		int quantity = 0;
		for (Item item : inventory.getItems())
		{
			if (item.getId() == nailType)
			{
				quantity = item.getQuantity();
				if (quantity == Integer.MAX_VALUE)
				{
					continue;
				}
				break;
			}
		}

		int diff = quantity - nailQuantity;
		nailQuantity = quantity;

		return diff;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!COLLAPSIBLE_MESSAGETYPES.contains(event.getType()))
		{
			return;
		}

		message = event.getMessage();
		messageTick = client.getTickCount();
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		if (updateNailQuantity() <= -1 && messageTick == client.getTickCount())
		{
			final int level = client.getBoostedSkillLevel(getSkill().getSkill()) + crystalSawBoost();

			if (NAIL_SUCCESS.equals(message))
			{
				update(level, 1, 0);
			}
			else if (NAIL_FAILURE.equals(message))
			{
				update(level, 0, 1);
			}
		}
	}
}
