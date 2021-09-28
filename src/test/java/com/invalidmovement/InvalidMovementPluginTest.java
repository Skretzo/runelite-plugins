package com.invalidmovement;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class InvalidMovementPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InvalidMovementPlugin.class);
		RuneLite.main(args);
	}
}