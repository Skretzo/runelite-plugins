package com.linemarkers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LineMarkerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LineMarkerPlugin.class);
		RuneLite.main(args);
	}
}