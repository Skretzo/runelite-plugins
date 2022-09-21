package com.chatsuccessrates;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatSuccessRatesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatSuccessRatesPlugin.class);
		RuneLite.main(args);
	}
}