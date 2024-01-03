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
package xrio.npcid;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("npcid")
public interface NpcIdConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "showId",
		name = "Show ID",
		description = "Show the NPC ID above the NPC."
	)
	default boolean showId()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "showIndex",
		name = "Show index",
		description = "Show the unique NPC index above the NPC."
	)
	default boolean showIndex()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "showName",
		name = "Show name",
		description = "Show the NPC name without combat level above the NPC."
	)
	default boolean showName()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hoverOnly",
		name = "Show on hover only",
		description = "Show the NPC identification text only when hovering the NPC."
	)
	default boolean hoverOnly()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "stripTags",
		name = "Strip tags",
		description = "Whether to strip NPC name formatting tags like &lt;col=00ffff&gt;&lt;/col&gt;."
	)
	default boolean stripTags()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "textColour",
		name = "Text colour",
		description = "The colour of the NPC identification text."
	)
	default Color textColour()
	{
		return Color.WHITE;
	}
}
