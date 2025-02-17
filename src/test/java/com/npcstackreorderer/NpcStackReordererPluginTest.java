package com.npcstackreorderer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NpcStackReordererPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NpcStackReordererPlugin.class);
		RuneLite.main(args);
	}
}