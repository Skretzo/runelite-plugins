package com.polygonlimiter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PolygonLimiterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PolygonLimiterPlugin.class);
		RuneLite.main(args);
	}
}