package com.tunnelvision;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TunnelVisionPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TunnelVisionPlugin.class);
		RuneLite.main(args);
	}
}