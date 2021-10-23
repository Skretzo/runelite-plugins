package com.successrates;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SuccessRatesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SuccessRatesPlugin.class);
		RuneLite.main(args);
	}
}