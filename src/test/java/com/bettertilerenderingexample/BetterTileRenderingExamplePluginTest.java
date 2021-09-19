package com.bettertilerenderingexample;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterTileRenderingExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BetterTileRenderingExamplePlugin.class);
		RuneLite.main(args);
	}
}