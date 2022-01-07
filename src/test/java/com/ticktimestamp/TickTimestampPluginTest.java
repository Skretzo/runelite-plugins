package com.ticktimestamp;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TickTimestampPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TickTimestampPlugin.class);
		RuneLite.main(args);
	}
}