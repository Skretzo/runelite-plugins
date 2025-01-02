/*
 * Copyright (c) 2021, Xrio
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.npcid;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(NpcIdConfig.GROUP)
public interface NpcIdConfig extends Config
{
	static final String GROUP = "npcid";

	@ConfigItem(
		position = 0,
		keyName = "showId",
		name = "Show ID",
		description = "Show the NPC ID above the NPC"
	)
	default boolean showId()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "showIndex",
		name = "Show index",
		description = "Show the unique NPC index above the NPC"
	)
	default boolean showIndex()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "showName",
		name = "Show name",
		description = "Show the NPC name without combat level above the NPC"
	)
	default boolean showName()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hoverOnly",
		name = "Show on hover only",
		description = "Show the NPC identification text only when hovering the NPC"
	)
	default boolean hoverOnly()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "showIdInMenu",
		name = "Show ID in menu",
		description = "Show the NPC ID in the right-click menu"
	)
	default boolean showIdInMenu()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "showIndexInMenu",
		name = "Show index in menu",
		description = "Show the unique NPC index in the right-click menu"
	)
	default boolean showIndexInMenu()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "stripTags",
		name = "Strip tags",
		description = "Whether to strip NPC name formatting tags like &lt;col=00ffff&gt;&lt;/col&gt;"
	)
	default boolean stripTags()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 7,
		keyName = "textColour",
		name = "Text colour",
		description = "The colour of the NPC identification text"
	)
	default Color textColour()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 8,
		keyName = "hidePets",
		name = "Hide pets",
		description = "Whether to hide NPC identification text for pets"
	)
	default boolean hidePets()
	{
		return false;
	}

	@ConfigItem(
		position = 9,
		keyName = "hideRandomEvents",
		name = "Hide random events",
		description = "Whether to hide NPC identification text for random event NPCs"
	)
	default boolean hideRandomEvents()
	{
		return false;
	}

	@ConfigItem(
		position = 10,
		keyName = "showTransmitOrder",
		name = "Show transmit order",
		description = "Whether to show NPC transmit order above the NPC." +
			"<br>The transmit order is the order in which the client sees the NPCs." +
			"<br>The first NPC that spawned or entered the scene will be 0." +
			"<br>The last NPC that respawned or entered the scene will have the highest number." +
			"<br>Ties are resolved by iterating 8x8 zones from south-west to north-east " +
			"<br>around the player, and the order in which the NPCs entered the 8x8 zones."
	)
	default boolean showTransmitOrder()
	{
		return false;
	}

	@ConfigItem(
		position = 11,
		keyName = "showTransmitOrderInMenu",
		name = "Show transmit order in menu",
		description = "Whether to show NPC transmit order in the right-click menu." +
			"<br>The transmit order is the order in which the client sees the NPCs." +
			"<br>The first NPC that spawned or entered the scene will be 0." +
			"<br>The last NPC that respawned or entered the scene will have the highest number." +
			"<br>Ties are resolved by iterating 8x8 zones from south-west to north-east " +
			"<br>around the player, and the order in which the NPCs entered the 8x8 zones." +
			"<br>The transmit order is only a snapshot of the game tick the menu was opened."
	)
	default boolean showTransmitOrderInMenu()
	{
		return false;
	}
}
