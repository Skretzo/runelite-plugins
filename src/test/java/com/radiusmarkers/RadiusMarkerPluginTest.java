package com.radiusmarkers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RadiusMarkerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RadiusMarkerPlugin.class);
		RuneLite.main(args);
	}
}