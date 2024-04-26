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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Set;
import net.runelite.api.MenuAction;
import net.runelite.api.NpcID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "NPC ID",
	description = "Display identification information as text above NPCs.",
	tags = {"NPC", "ID", "index", "name"}
)
public class NpcIdPlugin extends Plugin
{
	private static final Set<Integer> NPC_MENU_ACTIONS = ImmutableSet.of(
		MenuAction.NPC_FIRST_OPTION.getId(),
		MenuAction.NPC_SECOND_OPTION.getId(),
		MenuAction.NPC_THIRD_OPTION.getId(),
		MenuAction.NPC_FOURTH_OPTION.getId(),
		MenuAction.NPC_FIFTH_OPTION.getId(),
		MenuAction.EXAMINE_NPC.getId()
	);
	static final Set<Integer> RANDOM_EVENT_NPC_IDS = ImmutableSet.of(
		NpcID.BEE_KEEPER_6747,
		NpcID.CAPT_ARNAV,
		NpcID.DR_JEKYLL, NpcID.DR_JEKYLL_314,
		NpcID.DRUNKEN_DWARF,
		NpcID.DUNCE_6749,
		NpcID.EVIL_BOB, NpcID.EVIL_BOB_6754,
		NpcID.FLIPPA_6744,
		NpcID.FREAKY_FORESTER_6748,
		NpcID.FROG_5429, NpcID.FROG_5430, NpcID.FROG_5431, NpcID.FROG_5432, NpcID.FROG, NpcID.FROG_PRINCE, NpcID.FROG_PRINCESS,
		NpcID.GENIE, NpcID.GENIE_327,
		NpcID.GILES, NpcID.GILES_5441,
		NpcID.LEO_6746,
		NpcID.MILES, NpcID.MILES_5440,
		NpcID.MYSTERIOUS_OLD_MAN_6750, NpcID.MYSTERIOUS_OLD_MAN_6751,
		NpcID.MYSTERIOUS_OLD_MAN_6752, NpcID.MYSTERIOUS_OLD_MAN_6753,
		NpcID.NILES, NpcID.NILES_5439,
		NpcID.PILLORY_GUARD,
		NpcID.POSTIE_PETE_6738,
		NpcID.QUIZ_MASTER_6755,
		NpcID.RICK_TURPENTINE, NpcID.RICK_TURPENTINE_376,
		NpcID.SANDWICH_LADY,
		NpcID.SERGEANT_DAMIEN_6743,
		NpcID.COUNT_CHECK_12551, NpcID.COUNT_CHECK_12552
	);
	public int hoverNpcIndex = -1;

	@Inject
	private NpcIdOverlay npcOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Provides
	NpcIdConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NpcIdConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(npcOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(npcOverlay);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (NPC_MENU_ACTIONS.contains(event.getType()))
		{
			hoverNpcIndex = event.getIdentifier();
		}
		else
		{
			hoverNpcIndex = -1;
		}
	}
}
