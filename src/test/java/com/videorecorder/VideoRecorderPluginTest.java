package com.videorecorder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VideoRecorderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VideoRecorderPlugin.class);
		RuneLite.main(args);
	}
}