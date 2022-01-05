package com.videorecorder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("videorecorder")
public interface VideoRecorderConfig extends Config
{
	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		keyName = "framerate",
		name = "FPS",
		description = "The framerate (frames/second) for the video.",
		position = 0
	)
	default int framerate()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "hotkeyStart",
		name = "Start video hotkey",
		description = "The hotkey that will start the video recording.<br>Alternatively use the start button in the plugin panel.",
		position = 1
	)
	default Keybind hotkeyStart()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "hotkeyStop",
		name = "Stop video hotkey",
		description = "The hotkey that will stop the video recording.<br>Alternatively use the stop button in the plugin panel.",
		position = 2
	)
	default Keybind hotkeyStop()
	{
		return Keybind.NOT_SET;
	}
}
