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

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NpcIdOverlay extends Overlay
{
	private final Client client;
	private final NpcIdPlugin plugin;

	@Inject
	NpcIdOverlay(Client client, NpcIdPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.showId && !plugin.showIndex && !plugin.showName)
		{
			return null;
		}

		for (NPC npc : client.getNpcs())
		{
			renderNpcOverlay(graphics, npc, plugin.textColour);
		}

		return null;
	}

	private void renderNpcOverlay(Graphics2D graphics, NPC npc, Color colour)
	{
		if (npc == null || npc.getId() < 0 || npc.getName() == null || npc.getName().isEmpty() || "null".equals(npc.getName()))
		{
			return;
		}

		if ((plugin.hoverOnly && plugin.hoverNpcIndex != npc.getIndex()) ||
			(plugin.hidePets && npc.getComposition().isFollower()) ||
			(plugin.hideRandomEvents && NpcIdPlugin.RANDOM_EVENT_NPC_IDS.contains(npc.getId())))
		{
			return;
		}

		String text = "";

		if (plugin.showName)
		{
			text += plugin.stripTags ? npc.getName().replaceAll("</?[=\\w]*>", "") : npc.getName();
		}

		if (plugin.showId)
		{
			text += (plugin.showName ? " " : "") + npc.getId();
		}

		if (plugin.showIndex)
		{
			text += (plugin.showName && !plugin.showId ? " " : "") + "#" + npc.getIndex();
		}

		final Point textLocation = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, colour);
		}
	}
}
