package com.npcstackreorderer;

import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;

@Getter
public class NpcInfo
{
	private final Actor actor;
	private final boolean size1x1;

	public NpcInfo(Player player)
	{
		this.actor = player;
		this.size1x1 = true;
	}

	public NpcInfo(NPC npc, int size)
	{
		this.actor = npc;
		this.size1x1 = size <= 1;
	}

	public boolean isNpc()
	{
		return actor instanceof NPC;
	}
}
